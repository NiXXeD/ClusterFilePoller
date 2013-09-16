package org.nixxed.clusterfilepoller.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

/**
 * 
 */
@Component
public class CuratorFrameworkFactoryBean {
    public PathChildrenCache createPathChildrenCache(CuratorFramework curatorFramework, String basePath) {
        return new PathChildrenCache(curatorFramework, basePath, true);
    }

    public LeaderLatch createLeaderLatch(CuratorFramework curatorFramework, String basePath) {
        return new LeaderLatch(curatorFramework, basePath);
    }
    
    public CuratorFramework createClient(String zookeeperUrl) {
        return CuratorFrameworkFactory.newClient(zookeeperUrl, new ExponentialBackoffRetry(5000, 3));
    }
}
