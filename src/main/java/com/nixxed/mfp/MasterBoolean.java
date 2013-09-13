package com.nixxed.mfp;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilize ZooKeeper to store a boolean setting within a cluster.
 */
public class MasterBoolean implements PathChildrenCacheListener {
	private final String zkPathRoot;
	private final String zkNodeDescription;
	private final CuratorFrameworkHelper curatorFrameworkHelper;

	private PathChildrenCache pathChildrenCache;
	private List<MasterBooleanListener> listeners = new ArrayList<MasterBooleanListener>();
	private String zkFullPath;
	private boolean currentValue;

	public List<MasterBooleanListener> getListeners() {
		return listeners;
	}
	
	public boolean getCurrentValue() {
		return currentValue;
	}

	public MasterBoolean(CuratorFrameworkHelper curatorFrameworkHelper, PathChildrenCache pathChildrenCache, String zkPathRoot, String zkNodeDescription) {
		this.curatorFrameworkHelper = curatorFrameworkHelper;
		this.pathChildrenCache = pathChildrenCache;
		this.zkPathRoot = zkPathRoot;
		this.zkNodeDescription = zkNodeDescription;
	}
	
	@PostConstruct
	public void start() throws Exception {
		zkFullPath = ZKPaths.makePath(zkPathRoot, zkNodeDescription);
		pathChildrenCache.getListenable().addListener(this);
		pathChildrenCache.start();
	}
	
	@PreDestroy
	public void stop() {
		try {
			if (pathChildrenCache != null) {
				pathChildrenCache.close();
			}
		} catch (Exception e) {
			//silent close
		}
	}

	/**
	 * Update the value of this ZK node.
	 * @param value The new value.
	 * @throws Exception
	 */
	public void setValue(boolean value) throws Exception {
		byte[] bytes = (value ? "1" : "0").getBytes();
		curatorFrameworkHelper.setValue(zkFullPath, bytes);
	}

	@Override
	public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
		ChildData childData = pathChildrenCacheEvent.getData();
		if (childData != null) {
			if (childData.getPath().equals(zkFullPath)) {
				String data = new String(childData.getData());
				currentValue = "1".equals(data);

				notifyListeners();
			}
		}
	}
	
	private void notifyListeners() {
		for(MasterBooleanListener listener : listeners) {
			listener.masterBooleanValueChanged(currentValue);
		}
	}
}
