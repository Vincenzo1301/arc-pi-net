package hm.edu.arc.pi.net.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

  @Bean
  public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor(
        r -> {
          Thread thread = new Thread(r, "BeaconSenderScheduler");
          thread.setDaemon(true);
          return thread;
        });
  }
}
