package hm.edu.arc.pi.net.service;

public interface RateAdaptionService {

  double obtainSendingInterval();

  void updateEstimatedNodeCount(int nodeCount);

  void updateAverageMessageSize(double newSize);
}
