package org.nixxed.clusterfilepoller.zk;

import org.nixxed.clusterfilepoller.timer.FilePollScheduler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Utilize ZooKeeper LeaderElection to start/stop a Quartz Scheduler based on leader status.
 * Additionally support pause/resume functionality across the cluster.
 */
public class ClusterScheduler implements LeaderLatchListener, PathChildrenCacheListener {
    private static final Logger logger = LoggerFactory.getLogger(ClusterScheduler.class);

	private final CuratorFramework curatorFramework;
	private final PathChildrenCache pathChildrenCache;
	private final LeaderLatch leaderLatch;
	private final FilePollScheduler scheduler;
	private final String electionPath;

	private String pausePath;
    private boolean paused;

    public ClusterScheduler(CuratorFramework curatorFramework, PathChildrenCache pathChildrenCache, 
                            LeaderLatch leaderLatch, FilePollScheduler scheduler, String electionPath) {
	    this.curatorFramework = curatorFramework;
	    this.pathChildrenCache = pathChildrenCache;
	    this.leaderLatch = leaderLatch;
	    this.scheduler = scheduler;
	    this.electionPath = electionPath;
    }

	/**
	 * Start this ClusterScheduler instance. This will connect to ZooKeeper and if this
	 * instance is identified as master, the scheduler will start.
	 * @throws Exception
	 */
    @PostConstruct
    public void start() throws Exception {
	    pausePath = ZKPaths.makePath(electionPath, "paused");
	    pathChildrenCache.getListenable().addListener(this);
	    pathChildrenCache.start();
	    
	    leaderLatch.addListener(this);
	    leaderLatch.start();
    }

	/**
	 * Stop this ClusterScheduler instance.
	 */
    @PreDestroy
    public void stop() {
	    try {
		    if (leaderLatch != null) {
			    leaderLatch.close();
		    }
	    } catch (IOException e) {
		    logger.info("Unable to shut down leader latch for electionPath={}", electionPath);
	    }

	    if (scheduler != null) {
		    scheduler.stop();
	    }
    }

    @Override
    public void isLeader() {
	    updateSchedulerState();
    }

    @Override
    public void notLeader() {
	    updateSchedulerState();
    }

    private void updateSchedulerState() {
        if (leaderLatch.hasLeadership() && !paused) {
			if (!scheduler.isStarted()) {
				logger.info("Starting scheduler for electionPath={}", electionPath);
				scheduler.start();
			}
        } else {
			if (scheduler.isStarted()) {
				logger.info("Stopping scheduler for electionPath={} due to leader change.", electionPath);
				scheduler.stop();
			}
        }
    }

	/**
	 * Pause this scheduler globally for this election path.
	 * @throws Exception
	 */
	public void pause() throws Exception {
		setValue("1");
	}

	/**
	 * Resume a paused scheduler globally for this election path.
	 * NOTE; This scheduler instance will not start unless it is master.
	 * @throws Exception
	 */
	public void resume() throws Exception {
		setValue("0");
	}

	/**
	 * Update the value of this ZK node.
	 * @param value The new value.
	 * @throws Exception
	 */
	public void setValue(String value) throws Exception {
		byte[] bytes = value.getBytes();
		try {
			curatorFramework.setData().forPath(pausePath, bytes);
		} catch (KeeperException.NoNodeException e) {
			curatorFramework.create().creatingParentsIfNeeded().forPath(pausePath, bytes);
		}
	}

	@Override
	public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
		ChildData childData = pathChildrenCacheEvent.getData();
		if (childData != null) {
			if (childData.getPath().equals(pausePath)) {
				paused = "1".equals(new String(childData.getData()));

				updateSchedulerState();
			}
		}
	}
}
