package org.nixxed.clusterfilepoller;

import org.nixxed.clusterfilepoller.timer.FilePollScheduler;
import org.nixxed.clusterfilepoller.zk.ClusterScheduler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ClusterFilePollerFactory {
	/**
	 * Create a new ClusterFilePoller. Can contain many jobs.
	 * @param zkConnectionString The connection string to the ZooKeeper instance for this node.
	 * @param listener The listener to call per-file. Note, this is *synchronous*. If you want concurrency within jobs, implement it yourself.
	 * @param jobs All of the jobs you want to run. Each will be its own thread.
	 * @return Your new ClusterFilePoller object.
	 */
	public static ClusterFilePoller createClusterFilePoller(String zkConnectionString, FilePollListener listener, ClusterFilePollJob...jobs) {
		FilePollScheduler scheduler = new FilePollScheduler(listener, jobs);

		CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkConnectionString, new ExponentialBackoffRetry(5000, 3));

		String electionPath = "/cfp";
		PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, electionPath, true);
		LeaderLatch leaderLatch = new LeaderLatch(curatorFramework, electionPath);
		
		ClusterScheduler clusterScheduler = new ClusterScheduler(curatorFramework, pathChildrenCache, leaderLatch, scheduler, electionPath);
		
		return new ClusterFilePoller(curatorFramework, clusterScheduler);
	}
}
