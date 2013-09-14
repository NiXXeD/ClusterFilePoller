ClusterFilePoller
================

A simple java file poller utility that works in a clustered environment with automated failover.
* Regex match against file names
* Poll many folders (threads)
* Pause/resume support (cluster-wide)


Dependencies
============
* Apache [ZooKeeper](http://zookeeper.apache.org) (Cluster configuration)
* Apache [Curator](http://curator.incubator.apache.org) (ZooKeeper client)
* [SLF4J](http://www.slf4j.org) (Logging)

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
