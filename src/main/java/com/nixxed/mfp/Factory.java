package com.nixxed.mfp;

import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;

/**
 * 
 */
public class Factory {
	public LeaderLatch createLeaderLatch(String electionPath) {
		return new LeaderLatch(curatorFramework, electionPath);
	}
	
	public PathChildrenCache createPathChildrenCache(String zkPath) {
		return new PathChildrenCache(curatorFramework, zkPath, true);
	}
	
	public MasterBoolean createMasterBoolean(String zkPath, String nodeDescription) {
		return new MasterBoolean(this, zkPath, nodeDescription);
	}
}
