package hm.edu.arc.pi.net.service.impl;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ScheduledExecutorService;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskExecutor {

  private ScheduledExecutorService scheduler;

  public void startScheduledTask(Runnable task, long initialDelay, long period) {
    try {
      scheduler = newSingleThreadScheduledExecutor();
      scheduler.scheduleAtFixedRate(task, initialDelay, period, SECONDS);
      System.out.println("Started scheduled task with period of " + period + " seconds");
    } catch (Exception e) {
      throw new RuntimeException("Failed to start scheduled task", e);
    }
  }

  public void stopScheduledTask() {
    if (scheduler != null) {
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(5, SECONDS)) {
          System.err.println("Scheduler did not terminate in time. Forcing shutdown...");
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        System.err.println("Interrupted while waiting for scheduler to terminate.");
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }
      System.out.println("Stopped scheduled task.");
    }
  }
}
