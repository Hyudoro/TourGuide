package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

public class Tracker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Tracker.class);
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
    // for better readability.
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
        r -> {
            Thread t = new Thread (r, "tracker");
            t.setDaemon(true);
            return t;
        });
    private final TourGuideService tourGuideService;
    private volatile boolean stop = false;

    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;

        executorService.submit(this);
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS))
                logger.warn("Tracker did not terminate within 5 seconds");
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }

            List<User> users = tourGuideService.getAllUsers();
            logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
            stopWatch.start();
            // We check per user not per pass.
            for (User user : users){
                if (stop) break;
                tourGuideService.trackUserLocation(user);
            }
            if (stop) break; // that way we don't fall into the polling sleep after being told to stop.
            stopWatch.stop();
            logger.debug("Tracker Time Elapsed: " + stopWatch.getDuration().toSeconds() + " seconds.");
            stopWatch.reset();
            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }

    }
}
