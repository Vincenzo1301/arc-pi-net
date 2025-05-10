package hm.edu.arc.pi.net.service.impl;

import static java.lang.Math.E;
import static java.lang.Math.max;

import hm.edu.arc.pi.net.service.RateAdaptionService;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RateAdaptionServiceImpl implements RateAdaptionService {

  private static final Logger logger = LoggerFactory.getLogger(RateAdaptionServiceImpl.class);

  private static final double MINIMUM_SENDING_INTERVAL = 1000;
  private static final double MAXIMUM_BANDWIDTH = 50000;
  private static final double INITIAL_SENDING_DELAY = 1000.0;
  private static final double CORRECTION_FACTOR = E - 1.5;

  private int estimatedNodeCount = 1;
  private double averageMessageSize = -1;
  private double lastTPrime = -1;

  private double sendingInterval;

  public RateAdaptionServiceImpl() {
    this.sendingInterval = INITIAL_SENDING_DELAY + calculateTPrime();
  }

  @Override
  public double obtainSendingInterval() {
    return this.sendingInterval;
  }

  @Override
  public void updateEstimatedNodeCount(int nodeCount) {
    logger.info("Updating estimated node count to {}", nodeCount);
    this.estimatedNodeCount = nodeCount;
    calculateSendingInterval();
  }

  @Override
  public void updateAverageMessageSize(double newSize) {
    logger.info("Updating average message size to {}", newSize);
    this.averageMessageSize = newSize;
    calculateSendingInterval();
  }

  private void calculateSendingInterval() {
    double newTPrime = calculateTPrime();
    double deltaT = newTPrime - this.lastTPrime;
    this.lastTPrime = newTPrime;

    if (deltaT <= 0) {
      this.sendingInterval = 0;
      logger.info("DeltaT <= 0, scheduling immediate transmission");
    } else {
      this.sendingInterval = max(MINIMUM_SENDING_INTERVAL, this.sendingInterval + deltaT);
      logger.info("DeltaT > 0, delaying transmission by {}", deltaT);
    }
  }

  private double calculateTPrime() {
    double t = calculateT();

    // Handle negative or zero values to prevent invalid random range.
    // When t is negative, lower (0.5 * t) would be greater than upper (1.5 * t),
    // which violates the requirement that bound must be greater than origin in
    // RandomGenerator.nextDouble()
    if (t <= 0) {
      return MINIMUM_SENDING_INTERVAL;
    }

    double lower = 0.5 * t;
    double upper = 1.5 * t;
    double randomized = ThreadLocalRandom.current().nextDouble(lower, upper);

    return max(MINIMUM_SENDING_INTERVAL, randomized / CORRECTION_FACTOR);
  }

  private double calculateT() {
    return (estimatedNodeCount * averageMessageSize) / MAXIMUM_BANDWIDTH;
  }
}
