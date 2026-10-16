package com.hotel.job;

import com.hotel.util.Mails;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Starts the reminder email job when Tomcat starts the app, and stops everything cleanly on shutdown. */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "happystay-reminders");
            t.setDaemon(true);
            return t;
        });
        // first run 1 minute after start-up, then every 15 minutes
        scheduler.scheduleWithFixedDelay(new ReminderJob(), 1, 15, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) scheduler.shutdownNow();
        Mails.shutdown();
    }
}
