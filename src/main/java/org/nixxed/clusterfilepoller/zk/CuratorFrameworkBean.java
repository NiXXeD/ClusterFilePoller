package org.nixxed.clusterfilepoller.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 
 */
@Component
public class CuratorFrameworkBean implements ConnectionStateListener {
    private static final Logger logger = LoggerFactory.getLogger(CuratorFrameworkBean.class);
    
    private final CuratorFrameworkFactoryBean factory;

    private CuratorFramework curatorFramework;

    @Autowired
    public CuratorFrameworkBean(CuratorFrameworkFactoryBean factory) {
        this.factory = factory;
    }

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    @PostConstruct
    public void start() throws Exception {
        String zookeeperUrl = System.getProperty("zookeeper.url");
        curatorFramework = factory.createClient(zookeeperUrl);
        curatorFramework.getConnectionStateListenable().addListener(this);
        curatorFramework.start();
        curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
    }

    @PreDestroy
    public void stop() {
        curatorFramework.close();
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
        logger.info("ZooKeeper connection state changed: state={}", newState);
    }

    public PathChildrenCache createPathChildrenCache(String basePath) {
        return factory.createPathChildrenCache(curatorFramework, basePath);
    }

    public LeaderLatch createLeaderLatch(String basePath) {
        return factory.createLeaderLatch(curatorFramework, basePath);
    }

}
