MasterFilePoller
================

A java file poller utility to work in a clustered environment with automated failover.


Dependencies
============
* Apache [ZooKeeper](http://zookeeper.apache.org)
* Apache [Curator](http://curator.incubator.apache.org)


Usage
=====
Maven:

    <dependency>
      <groupId>org.nixxed</groupId>
      <artifactId>clusterfilepoller</artifactId>
      <version>0.0.2</version>
    </dependency>
    
Code:
    
    //look for xml and zip files
    ClusterFilePollJob job = new ClusterFilePollJob("/some/path", "(?i)^.*(xml|zip)$", 5);
    ClusterFilePoller poller = ClusterFilePollerFactory.createClusterFilePoller("localhost:2181", 
        new FilePollListener() {
			public void fileFound(File file) {
				System.out.println("Found: " + file.getPath());
			}
		}, job);
	
	poller.start();
	
	//do other work on this thread, etc
	
	poller.stop();
