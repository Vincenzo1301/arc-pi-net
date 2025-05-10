package hm.edu.arc.pi.net.service.impl;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import hm.edu.arc.pi.net.data.Beacon;
import hm.edu.arc.pi.net.service.BeaconSender;
import hm.edu.arc.pi.net.service.RateAdaptionService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BeaconSenderImpl implements BeaconSender {

  private static final Logger logger = LoggerFactory.getLogger(BeaconSenderImpl.class);

  private static final String BROADCAST_IP = "192.168.42.255";
  private static final int PORT = 12345;
  private static final String INTERFACE_NAME = "en0";
  private static final int DELAY = 0;
  private static final String NODE_ID = UUID.randomUUID().toString();

  private final RateAdaptionService rateAdaptionService;
  private final ObjectMapper objectMapper;
  private final DatagramSocket socket;
  private final InetAddress broadcastAddress;
  private final ScheduledExecutorService scheduler;

  private ScheduledFuture<?> beaconTask;
  private volatile boolean running = false;

  @Autowired
  public BeaconSenderImpl(
      RateAdaptionService rateAdaptionService,
      ObjectMapper objectMapper,
      ScheduledExecutorService scheduler)
      throws Exception {
    this.rateAdaptionService = rateAdaptionService;
    this.objectMapper = objectMapper;
    this.scheduler = scheduler;
    this.socket = new DatagramSocket(null);
    this.socket.setBroadcast(true);

    NetworkInterface wlan = NetworkInterface.getByName(INTERFACE_NAME);
    if (wlan == null) {
      logger.error("Interface {} not found", INTERFACE_NAME);
      throw new RuntimeException(format("Interface %s not found", INTERFACE_NAME));
    }

    InetAddress wlanAddress = wlan.getInetAddresses().nextElement();
    socket.bind(new InetSocketAddress(wlanAddress, 0));
    this.broadcastAddress = InetAddress.getByName(BROADCAST_IP);

    logger.info("BeaconSender initialized");
  }

  @Override
  public void startSending() {
    if (running) {
      logger.warn("BeaconSender is already running");
      return;
    }

    running = true;
    double sendingInterval = rateAdaptionService.obtainSendingInterval();

    beaconTask =
        scheduler.scheduleAtFixedRate(
            this::sendBeacon, DELAY, (long) sendingInterval, TimeUnit.MILLISECONDS);

    logger.info("BeaconSender started with initial interval of {} ms", sendingInterval);
  }

  @Override
  public void stopSending() {
    if (!running) {
      logger.warn("BeaconSender is not running");
      return;
    }

    running = false;
    if (beaconTask != null) {
      beaconTask.cancel(false);
      beaconTask = null;
    }
    logger.info("BeaconSender stopped");
  }

  private List<Beacon> generateDummyBeacons() {
    int[] dummyMeasurements = new int[100];
    for (int i = 0; i < dummyMeasurements.length; i++) {
      dummyMeasurements[i] = (int) (Math.random() * 100);
    }

    Beacon beacon = new Beacon(1, NODE_ID, Instant.now().toEpochMilli(), 1, dummyMeasurements);

    // Only one beacon for now for now to send all measurements.
    return Collections.singletonList(beacon);
  }

  private void sendBeacon() {
    try {
      Beacon beacon = generateDummyBeacons().get(0);
      String beaconJson = objectMapper.writeValueAsString(beacon);
      byte[] data = beaconJson.getBytes(StandardCharsets.UTF_8);
      DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, PORT);
      socket.send(packet);

      // Update the interval as it could be changed in the meantime
      double newInterval = rateAdaptionService.obtainSendingInterval();
      if (beaconTask != null && running) {
        beaconTask.cancel(false);
        if (newInterval <= 0) {
          // For zero or negative intervals, send immediately and then reschedule.
          scheduler.schedule(this::sendBeacon, DELAY, TimeUnit.MILLISECONDS);
          logger.debug("Beacon scheduled for immediate sending");
        } else {
          beaconTask =
              scheduler.scheduleAtFixedRate(
                  this::sendBeacon, DELAY, (long) newInterval, TimeUnit.MILLISECONDS);
          logger.debug("Beacon sending interval updated to {} ms", newInterval);
        }
      }

      logger.debug("Beacon sent: {}", beaconJson);
    } catch (Exception e) {
      logger.error("Error sending beacon", e);
    }
  }
}
