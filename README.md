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
    <dependency>
      <groupId>org.apache.curator</groupId>
      <artifactId>curator-recipes</artifactId>
      <version>2.2.0-incubating</version>
    </dependency>
    
Spring version:

    <!--Create one or more of these (tied to a job). These will receive the file events.-->
    <bean id="sampleListener" class="org.nixxed.clusterfilepoller.sample.SampleListener"/>

    <!--Create one or more of these. Each one will receive its own processing thread.-->
    <bean class="org.nixxed.clusterfilepoller.ClusterFilePollJob">
        <constructor-arg name="path" value="/some/path"/>
        <constructor-arg name="regex" value="(?i)^.*(xml|zip)$"/>
        <constructor-arg name="intervalSeconds" value="5"/>
        <constructor-arg name="listener" ref="sampleListener"/>
    </bean>
    
    <!--Create one of these... Spring configuration class wiring beans together for us.-->
    <bean class="org.nixxed.clusterfilepoller.ClusterFilePollerFactory" />
    
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
	
