package com.nixxed.mfp;

import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Utilize ZooKeeper LeaderElection to start/stop a Quartz Scheduler based on leader status.
 * Additionally support pause/resume functionality across the cluster.
 */
public class MasterScheduler implements LeaderLatchListener, MasterBooleanListener {
    private static final Logger logger = LoggerFactory.getLogger(MasterScheduler.class);

	private final Factory factoryHelper;
	private final Scheduler scheduler;
	private final MasterBoolean masterBoolean;
	private final String electionPath;

    private LeaderLatch leaderLatch;
    private boolean paused;

    public MasterScheduler(Factory factoryHelper, Scheduler scheduler, MasterBoolean masterBoolean, String electionPath) {
	    this.factoryHelper = factoryHelper;
	    this.scheduler = scheduler;
	    this.masterBoolean = masterBoolean;
	    this.electionPath = electionPath;
    }

	/**
	 * Start this MasterScheduler instance. This will connect to ZooKeeper and if this
	 * instance is identified as master, the scheduler will start.
	 * @throws Exception
	 */
    @PostConstruct
    public void start() throws Exception {
	    leaderLatch = factoryHelper.createLeaderLatch(electionPath);
	    leaderLatch.addListener(this);
	    leaderLatch.start();
    }

	/**
	 * Stop this MasterScheduler instance.
	 */
    @PreDestroy
    public void stop() {
		try {
			if (scheduler != null) {
				scheduler.shutdown();
			}
		} catch (Exception e) {
			logger.info("Unable to shut down scheduler for electionPath={}", electionPath);
		}
	    
	    try {
		    if (leaderLatch != null) {
			    leaderLatch.close();
		    }
	    } catch (IOException e) {
		    logger.info("Unable to shut down leader latch for electionPath={}", electionPath);
	    }
    }

	/**
	 * Pause this scheduler globally for this election path.
	 * @throws Exception
	 */
	public void pause() throws Exception {
		setNodeValue("1".getBytes());
	}

	/**
	 * Resume a paused scheduler globally for this election path.
	 * NOTE; This scheduler instance will not start unless it is master.
	 * @throws Exception
	 */
	public void resume() throws Exception {
		setNodeValue("0".getBytes());
	}

    @Override
    public void isLeader() {
	    updateSchedulerState();
    }

    @Override
    public void notLeader() {
	    updateSchedulerState();
    }

    private void updateSchedulerState() {
        try {
            if (leaderLatch.hasLeadership() && !paused) {
				if (scheduler.isInStandbyMode() || !scheduler.isStarted()) {
					logger.info("Starting scheduler for electionPath={}", electionPath);
				}
            } else {
				if (scheduler.isStarted()) {
					logger.info("Stopping scheduler for electionPath={} due to leader change.", electionPath);
					scheduler.standby();
				}
            }
        } catch (SchedulerException e) {
	        logger.warn("Unable to start/pause scheduler for electionPath={}", electionPath);
        }
    }


	@Override
	public void masterBooleanValueChanged(boolean value) {
		
	}
}
