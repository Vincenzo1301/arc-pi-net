package hm.edu.arc.pi.net.service;

public interface NodeEstimatorService {

  void registerBeacon(String nodeId);

  int estimateNodeCount();
}
