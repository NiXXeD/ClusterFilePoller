package org.nixxed.clusterfilepoller;

import org.nixxed.clusterfilepoller.zk.ClusterScheduler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A clustered file poller. Utilizes ZooKeeper to handle cluster failover.
 */
public class ClusterFilePoller implements ConnectionStateListener {
	private static final Logger logger = LoggerFactory.getLogger(ClusterFilePoller.class);
	
	private final CuratorFramework curatorFramework;
	private final ClusterScheduler clusterScheduler;

	public ClusterFilePoller(CuratorFramework curatorFramework, ClusterScheduler clusterScheduler) {
		this.curatorFramework = curatorFramework;
		this.clusterScheduler = clusterScheduler;
	}

	public void start() throws Exception {
		curatorFramework.getConnectionStateListenable().addListener(this);
		curatorFramework.start();
		curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
		
		clusterScheduler.start();
	}
	
	public void stop() {
		clusterScheduler.stop();
		curatorFramework.close();
	}
	
	public void pause() throws Exception {
		clusterScheduler.pause();
	}
	
	public void resume() throws Exception {
		clusterScheduler.resume();
	}

	@Override
	public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
		logger.info("ZooKeeper connection state changed: state={}", newState);
	}
	
}
