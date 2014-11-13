package org.nixxed.clusterfilepoller.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class MasterHelper implements LeaderLatchListener, ConnectionStateListener, PathChildrenCacheListener {
    private static final Logger logger = LoggerFactory.getLogger(MasterHelper.class);

    private final String connectionString;
    private final String electionPath;
    private final String description;
    private final Mode mode;
    private final MasterTask task;

    private CuratorFramework curatorFramework;
    private LeaderLatch leaderLatch;
    private boolean globallyDisabled = false;
    private boolean enabled = false;

    public MasterHelper(MasterTask task, String description) {
        this.task = task;
        this.connectionString = null;
        this.electionPath = null;
        this.description = description;
        this.mode = Mode.Singleton;
    }

    public MasterHelper(MasterTask task, String connectionString, String electionPath, String description) {
        this.task = task;
        this.connectionString = connectionString;
        this.electionPath = electionPath;
        this.description = description;
        this.mode = Mode.Cluster;
    }

    @PostConstruct
    public void start() throws Exception {
        if(mode == Mode.Cluster) {
            curatorFramework = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(5000, 3));
            curatorFramework.getConnectionStateListenable().addListener(this);
            curatorFramework.start();
            curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();

            PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, electionPath, true);
            pathChildrenCache.getListenable().addListener(this);
            pathChildrenCache.start();

            leaderLatch = new LeaderLatch(curatorFramework, electionPath);
            leaderLatch.addListener(this);
            leaderLatch.start();
        } else if (mode == Mode.Singleton) {
            checkState();
        }

        logger.info("MasterHelper enabled={} mode={} description={}", enabled, mode, description);
    }

    @PreDestroy
    public void stop() {
        if (enabled) {
            task.stop();
        }
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
        logger.debug("ZooKeeper state={} description={}", newState, description);
    }

    @Override
    public void isLeader() {
        checkState();
    }

    public void notLeader() {
        checkState();
    }

    private synchronized void checkState() {
        if (mode == Mode.Cluster) {
            if (leaderLatch.hasLeadership() && !globallyDisabled && !enabled) {
                task.start();
                enabled = true;
                logger.info("MasterHelper enabled={} mode={} globallyDisabled={} description={}", true, mode, globallyDisabled, description);
            } else if ((!leaderLatch.hasLeadership() || globallyDisabled) && enabled) {
                task.stop();
                enabled = false;
                logger.info("MasterHelper enabled={} mode={} globallyDisabled={} description={}", false, mode, globallyDisabled, description);
            }
        } else if (mode == Mode.Singleton) {
            if (!globallyDisabled && !enabled) {
                task.start();
                enabled = true;
                logger.info("MasterHelper enabled={} mode={} globallyDisabled={} description={}", true, mode, globallyDisabled, description);
            } else {
                task.stop();
                enabled = false;
                logger.info("MasterHelper enabled={} mode={} globallyDisabled={} description={}", false, mode, globallyDisabled, description);
            }
        }
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        ChildData childData = event.getData();
        if (childData != null && childData.getPath().endsWith("disableGlobally")) {
            String data = new String(childData.getData());
            globallyDisabled = "1".equals(data);

            checkState();
        }
    }

    public synchronized void disableGlobally() throws Exception {
        if (leaderLatch != null) {
            setNodeValue("1".getBytes());
        } else {
            globallyDisabled = true;
            checkState();
        }
    }

    public synchronized void enableGlobally() throws Exception {
        if (leaderLatch != null) {
            setNodeValue("0".getBytes());
        } else {
            globallyDisabled = false;
            checkState();
        }
    }

    private void setNodeValue(byte[] bytes) throws Exception {
        String path = ZKPaths.makePath(electionPath, "disableGlobally");
        try {
            curatorFramework.setData().forPath(path, bytes);
        } catch (KeeperException.NoNodeException e) {
            curatorFramework.create().creatingParentsIfNeeded().forPath(path, bytes);
        }
    }

    private enum Mode {
        Cluster,
        Singleton
    }
}
