package org.nixxed.clusterfilepoller;

import org.nixxed.clusterfilepoller.timer.FilePollScheduler;
import org.nixxed.clusterfilepoller.zk.CuratorFrameworkBean;
import org.nixxed.clusterfilepoller.zk.CuratorFrameworkFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class ClusterFilePollerFactory {
    /**
     * Create a new ClusterFilePoller. Can contain many jobs.
     *
     * @param jobs All of the jobs you want to run. Each will be its own thread.
     * @return Your new ClusterFilePoller object.
     */
    public static ClusterFilePoller createClusterFilePoller(boolean autoStart, ClusterFilePollJob... jobs) throws Exception {
        FilePollScheduler scheduler = new FilePollScheduler();
        scheduler.setJobs(new HashSet<ClusterFilePollJob>(Arrays.asList(jobs)));

        CuratorFrameworkBean curatorFramework = new CuratorFrameworkBean(new CuratorFrameworkFactoryBean());

        ClusterFilePoller clusterFilePoller = new ClusterFilePoller();
        clusterFilePoller.setCuratorFramework(curatorFramework);
        clusterFilePoller.setScheduler(scheduler);
        if (autoStart) {
            clusterFilePoller.start();
        }
        return clusterFilePoller;
    }

    @Bean
    public FilePollScheduler getFilePollScheduler() {
        return new FilePollScheduler();
    }

    @Bean
    public CuratorFrameworkFactoryBean getCuratorFrameworkFactoryBean() {
        return new CuratorFrameworkFactoryBean();
    }

    @Bean
    public CuratorFrameworkBean getClusterFrameworkBean() {
        return new CuratorFrameworkBean(getCuratorFrameworkFactoryBean());
    }

    @Bean
    public ClusterFilePoller getClusterFilePoller() {
        return new ClusterFilePoller();
    }
}
