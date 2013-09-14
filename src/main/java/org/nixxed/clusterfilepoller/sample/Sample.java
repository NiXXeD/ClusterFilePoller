package org.nixxed.clusterfilepoller.sample;

import org.nixxed.clusterfilepoller.ClusterFilePollJob;
import org.nixxed.clusterfilepoller.ClusterFilePoller;
import org.nixxed.clusterfilepoller.ClusterFilePollerFactory;
import org.nixxed.clusterfilepoller.FilePollListener;

import java.io.File;

public class Sample {
	public static void main(String[] args) throws Exception {
		System.out.println("Started.");
		
		ClusterFilePollJob job = new ClusterFilePollJob("C://cygwin//home//NiXXeD//testpath", "(?i)^.*(xml|zip)$", 5);
		ClusterFilePoller poller = ClusterFilePollerFactory.createClusterFilePoller("localhost:2181", new FilePollListener() {
			public void fileFound(File file) {
				System.out.println("Found: " + file.getPath());
			}
		}, job);
		
		//start polling
		poller.start();
		Thread.sleep(10000);
		
		//sample pause/resume
		poller.pause();
		System.out.println("Paused.");
		Thread.sleep(10000);
		poller.resume();
		System.out.println("Resumed.");
		
		//stop and exit
		poller.stop();
		System.out.println("Stopped.");
	}
}
