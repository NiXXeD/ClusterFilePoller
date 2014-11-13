ClusterFilePoller
================

A simple java file poller utility that works in a clustered environment with automated failover.
* Regex match against file names
* Poll many folders (threads)
* Pause/resume support (cluster-wide)


Dependencies
============
* Apache [ZooKeeper](http://zookeeper.apache.org) (Cluster configuration)
* Apache [Curator](http://curator.apache.org/) (ZooKeeper client)
* [SLF4J](http://www.slf4j.org) (Logging)


Usage
=====

    //create a listener for our poller
    //consider using a thread pool for doing any work in this listener
    FilePollListener listener = new FilePollListener() {
        @Override
        public void fileFound(File file) {
            file.renameTo(new File(file.getPath() + "_running"));
        }
    };

    //create a job describing the path/regex to poll
    FilePollJob job = new FilePollJob("/test/path", "(?i)^.*\\.(txt|zip)$", 5, listener);

    //scheduler to manage our job(s), you can submit multiple here
    FilePollScheduler scheduler = new FilePollScheduler(job);

    //start a master helper, which orchestrates the whole process
    String connectionString = "localhost:2181";
    String electionPath = "/clusterFilePollerTest";
    String description = "TestPoller";
    MasterHelper masterHelper = new MasterHelper(scheduler, connectionString, electionPath, description);
    masterHelper.start();

Advanced Usage
==============

    //make a class that implements MasterTask
    //this task is called start/stop as necessary as master is passed around
    MasterTask task = new MasterTask() {
        @Override
        public void start() {
            logger.debug("CustomTask started...");
        }

        @Override
        public void stop() {
            logger.debug("CustomTask stopped...");
        }
    };

    MasterHelper masterHelper = new MasterHelper(task, connectionString, electionPath, description);
    masterHelper.start();