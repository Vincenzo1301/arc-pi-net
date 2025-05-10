package hm.edu.arc.pi.net.service.impl;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hm.edu.arc.pi.net.data.Beacon;
import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.BeaconReceiver;
import hm.edu.arc.pi.net.service.LogService;
import hm.edu.arc.pi.net.service.MessageSizeService;
import hm.edu.arc.pi.net.service.NodeEstimatorService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BeaconReceiverImpl implements BeaconReceiver, Runnable {

  private static final Logger logger = LoggerFactory.getLogger(BeaconReceiverImpl.class);

  private static final int PORT = 12345;
  private static final int BUFFER_SIZE = 2048;
  private static final String INTERFACE_NAME = "en0";
  private static final int SHUTDOWN_TIMEOUT_MILLIS = 2000;

  private final NodeEstimatorService nodeEstimatorService;
  private final RateAdaptionServiceImpl rateAdaptionService;
  private final MessageSizeService messageSizeService;
  private final LogService logService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private Thread receiverThread;
  private volatile boolean running = false;

  // Package-private accessors for testing
  boolean isRunning() {
    return running;
  }

  Thread getReceiverThread() {
    return receiverThread;
  }

  public BeaconReceiverImpl(
      NodeEstimatorService nodeEstimatorService,
      RateAdaptionServiceImpl rateAdaptionService,
      MessageSizeService messageSizeService,
      LogService logService) {
    this.nodeEstimatorService = nodeEstimatorService;
    this.rateAdaptionService = rateAdaptionService;
    this.messageSizeService = messageSizeService;
    this.logService = logService;
  }

  @Override
  public void startReceiving() {
    logger.info("Trying to start BeaconReceiver...");
    if (running) {
      logger.warn("BeaconReceiver is already running");
      return;
    }

    receiverThread = new Thread(this, "BeaconReceiverThread");
    receiverThread.start();
    running = true;
    logger.info("BeaconReceiver started");
  }

  @Override
  public void stopReceiving() {
    logger.info("Trying to stop BeaconReceiver...");
    if (!running) {
      logger.warn("BeaconReceiver is not running");
      return;
    }

    if (receiverThread != null) {
      receiverThread.interrupt();
      try {
        receiverThread.join(SHUTDOWN_TIMEOUT_MILLIS);
      } catch (InterruptedException e) {
        currentThread().interrupt();
      }

      running = false;
      logger.info("BeaconReceiver stopped");
    }
  }

  @Override
  public void run() {
    try (DatagramSocket socket = new DatagramSocket(null)) {
      NetworkInterface wlan = NetworkInterface.getByName(INTERFACE_NAME);
      if (wlan == null) {
        logger.error("Interface {} not found", INTERFACE_NAME);
        throw new RuntimeException(format("Interface %s not found", INTERFACE_NAME));
      }

      InetAddress wlanAddress = wlan.getInetAddresses().nextElement();
      socket.bind(new InetSocketAddress(wlanAddress, PORT));
      logger.info("BeaconReceiver listening on {}:{}", wlanAddress.getHostAddress(), PORT);

      byte[] buffer = new byte[BUFFER_SIZE];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
      while (running && !currentThread().isInterrupted()) {
        try {
          socket.receive(packet);
          handleMessage(packet);
        } catch (Exception e) {
          if (running) {
            logger.error("Error processing received packet", e);
          }
        }
      }
    } catch (Exception e) {
      if (running) {
        logger.error("Fatal error in BeaconReceiver", e);
      }
    }
  }

  void handleMessage(DatagramPacket packet) throws JsonProcessingException {
    //
    // region: register the beacon with the node estimator and update the estimated node count
    //
    String json = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
    Beacon beacon = objectMapper.readValue(json, Beacon.class);
    logger.debug("Received Beacon from {} at {}", beacon.sourceId(), beacon.timestamp());
    nodeEstimatorService.registerBeacon(beacon.sourceId());
    int estimateNodeCount = nodeEstimatorService.estimateNodeCount();
    //
    // endregion
    //

    //
    // region: update the estimated node count in the rate adaptation service
    //
    rateAdaptionService.updateEstimatedNodeCount(estimateNodeCount);
    //
    // endregion
    //

    //
    // region: update message size statistics
    //
    messageSizeService.calculateAverageMessageSize(packet.getLength());
    //
    // endregion
    //

    //
    // region: log the reception of the beacon
    //
    logService.addLog(new Log(beacon.sourceId(), beacon.timestamp()));
    //
    // endregion
    //
  }
}
