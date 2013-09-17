package org.nixxed.clusterfilepoller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.nixxed.clusterfilepoller.timer.FilePollScheduler;
import org.nixxed.clusterfilepoller.zk.CuratorFrameworkBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Utilize ZooKeeper LeaderElection to start/stop a scheduler based on leader status.
 * Additionally support pause/resume functionality across the cluster.
 */
public class ClusterFilePoller implements LeaderLatchListener, PathChildrenCacheListener {
    private static final Logger logger = LoggerFactory.getLogger(ClusterFilePoller.class);
    private static final String electionPath = "/cfp";

    private CuratorFrameworkBean curatorFramework;
    private FilePollScheduler scheduler;
    private PathChildrenCache pathChildrenCache;
    private LeaderLatch leaderLatch;
    private String pausePath;
    private boolean paused;

    @Autowired
    public void setCuratorFramework(CuratorFrameworkBean curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @Autowired
    public void setScheduler(FilePollScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Start this ClusterScheduler instance. This will connect to ZooKeeper and if this
     * instance is identified as master, the scheduler will start.
     *
     * @throws Exception
     */
    @PostConstruct
    public void start() throws Exception {
        logger.trace("ClusterFilePoller starting...");
        curatorFramework.start();

        pausePath = ZKPaths.makePath(electionPath, "paused");
        pathChildrenCache = curatorFramework.createPathChildrenCache(electionPath);
        pathChildrenCache.getListenable().addListener(this);
        pathChildrenCache.start();

        leaderLatch = curatorFramework.createLeaderLatch(electionPath);
        leaderLatch.addListener(this);
        leaderLatch.start();
        logger.trace("ClusterFilePoller started.");
    }

    /**
     * Stop this ClusterScheduler instance.
     */
    @PreDestroy
    public void stop() {
        logger.trace("ClusterFilePoller stopping...");
        try {
            if (leaderLatch != null) {
                leaderLatch.close();
            }
        } catch (IOException e) {
            logger.info("Unable to shut down leader latch for electionPath={}", electionPath);
        }

        try {
            if (pathChildrenCache != null) {
                pathChildrenCache.close();
            }
        } catch (IOException e) {
            logger.info("Unable to shut down node cache for electionPath={}", electionPath);
        }

        if (scheduler != null) {
            scheduler.stop();
        }
        logger.trace("ClusterFilePoller stopped.");
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
     *
     * @throws Exception
     */
    public void pause() throws Exception {
        setValue("1");
    }

    /**
     * Resume a paused scheduler globally for this election path.
     * NOTE; This scheduler instance will not start unless it is master.
     *
     * @throws Exception
     */
    public void resume() throws Exception {
        setValue("0");
    }

    /**
     * Update the value of this ZK node.
     *
     * @param value The new value.
     * @throws Exception
     */
    public void setValue(String value) throws Exception {
        byte[] bytes = value.getBytes();
        try {
            curatorFramework.getCuratorFramework().setData().forPath(pausePath, bytes);
        } catch (KeeperException.NoNodeException e) {
            curatorFramework.getCuratorFramework().create().creatingParentsIfNeeded().forPath(pausePath, bytes);
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
