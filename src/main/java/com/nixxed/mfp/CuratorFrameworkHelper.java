package com.nixxed.mfp;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Manage a CuratorFramework connection easily.
 */
public class CuratorFrameworkHelper implements ConnectionStateListener {
	private static final Logger logger = LoggerFactory.getLogger(CuratorFrameworkHelper.class);
	
	private CuratorFramework curatorFramework;
	private final String zkConnectionString;

	public CuratorFrameworkHelper(CuratorFramework curatorFramework, String zkConnectionString) {
		this.curatorFramework = curatorFramework;
		this.zkConnectionString = zkConnectionString;
	}

	@PostConstruct
	public void start() throws Exception {
		curatorFramework = org.apache.curator.framework.CuratorFrameworkFactory.newClient(zkConnectionString, new ExponentialBackoffRetry(5000, 3));
		curatorFramework.getConnectionStateListenable().addListener(this);
		curatorFramework.start();
		curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
	}

	@PreDestroy
	public void stop() {
		if (curatorFramework != null) {
			curatorFramework.close();
		}
	}

	@Override
	public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
		logger.info("ZooKeeper connection state changed: state={}", newState);
	}

	public void setValue(String zkPath, byte[] bytes) throws Exception {
		try {
			curatorFramework.setData().forPath(zkPath, bytes);
		} catch (KeeperException.NoNodeException e) {
			curatorFramework.create().creatingParentsIfNeeded().forPath(zkPath, bytes);
		}
	}
}
