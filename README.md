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
    
Spring version:

    <bean id="sampleListener" class="org.nixxed.clusterfilepoller.sample.SampleListener"/>
    
    <bean class="org.nixxed.clusterfilepoller.ClusterFilePollJob">
        <constructor-arg name="path" value="/some/path"/>
        <constructor-arg name="regex" value="(?i)^.*(xml|zip)$"/>
        <constructor-arg name="intervalSeconds" value="5"/>
        <constructor-arg name="listener" ref="sampleListener"/>
    </bean>
    
Code version:
    
    //look for xml and zip files every 5 seconds in /some/path
    ClusterFilePollJob job = new ClusterFilePollJob("/some/path", "(?i)^.*(xml|zip)$", 5, 
            new FilePollListener() {
                public void fileFound(File file) {
                    System.out.println("Found: " + file.getPath());
                }
            }
    );
    
    //true for autostart
    ClusterFilePoller poller = ClusterFilePollerFactory.createClusterFilePoller(true, job);
	
	//do other work on this thread, etc
	//...
	
	//stop when we're done
	poller.stop();