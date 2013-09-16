package org.nixxed.clusterfilepoller;

import org.nixxed.clusterfilepoller.timer.FilePollScheduler;
import org.nixxed.clusterfilepoller.zk.CuratorFrameworkBean;
import org.nixxed.clusterfilepoller.zk.CuratorFrameworkFactoryBean;

import java.util.Arrays;
import java.util.HashSet;

public class ClusterFilePollerFactory {
	/**
	 * Create a new ClusterFilePoller. Can contain many jobs.
	 * @param jobs All of the jobs you want to run. Each will be its own thread.
	 * @return Your new ClusterFilePoller object.
	 */
	public static ClusterFilePoller createClusterFilePoller(boolean autoStart, ClusterFilePollJob...jobs) throws Exception {
		FilePollScheduler scheduler = new FilePollScheduler();
        scheduler.setJobs(new HashSet<ClusterFilePollJob>(Arrays.asList(jobs)));

		CuratorFrameworkBean curatorFramework = new CuratorFrameworkBean(new CuratorFrameworkFactoryBean());
		
		ClusterFilePoller clusterFilePoller = new ClusterFilePoller(curatorFramework, scheduler);
        if (autoStart) {
            clusterFilePoller.start();
        }
        return clusterFilePoller;
	}
}
