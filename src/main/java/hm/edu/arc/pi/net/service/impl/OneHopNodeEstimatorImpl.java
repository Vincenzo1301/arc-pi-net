package hm.edu.arc.pi.net.service.impl;

import static java.time.Instant.now;

import hm.edu.arc.pi.net.service.NodeEstimatorService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OneHopNodeEstimatorImpl implements NodeEstimatorService {

  private static final long WINDOW_MILLIS = 1000;

  private final Map<String, Long> neighborTimestamps = new ConcurrentHashMap<>();

  @Override
  public void registerBeacon(String nodeId) {
    neighborTimestamps.put(nodeId, now().toEpochMilli());
  }

  @Override
  public int estimateNodeCount() {
    long now = now().toEpochMilli();
    return (int)
        neighborTimestamps.entrySet().stream()
            .filter(e -> now - e.getValue() <= WINDOW_MILLIS)
            .map(Map.Entry::getKey)
            .distinct()
            .count();
  }
}
