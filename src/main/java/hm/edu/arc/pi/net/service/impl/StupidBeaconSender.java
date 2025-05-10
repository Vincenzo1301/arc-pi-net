package hm.edu.arc.pi.net.service.impl;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import hm.edu.arc.pi.net.service.BeaconSender;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * StupidBeaconSender is a simple implementation of BeaconSender that sends a fixed message to a
 * broadcast address at regular intervals.
 *
 * <p>This class is not intended for real experiments of the ARC-DSA algorithm and is primarily to
 * test whether the network stack is working correctly.
 */
@Service
public class StupidBeaconSender implements BeaconSender {

  private static final Logger logger = LoggerFactory.getLogger(StupidBeaconSender.class);
  private static final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor();

  private static final int PORT = 12345;
  private static final String BROADCAST_IP = "255.255.255.255";
  private static final String MESSAGE = "Hello from StupidBeaconSender!";

  private DatagramSocket socket;

  @Override
  public void startSending() {
    try {
      socket = new DatagramSocket();
      socket.setBroadcast(true);

      scheduler.scheduleAtFixedRate(
          () -> {
            try {
              byte[] data = MESSAGE.getBytes(StandardCharsets.UTF_8);
              DatagramPacket packet =
                  new DatagramPacket(data, data.length, InetAddress.getByName(BROADCAST_IP), PORT);
              socket.send(packet);
              logger.info("Sent message: {}", MESSAGE);
            } catch (Exception e) {
              logger.error("Error sending packet", e);
            }
          },
          0,
          1,
          TimeUnit.SECONDS);
      logger.info("StupidBeaconSender started");
    } catch (Exception e) {
      logger.error("Error starting StupidBeaconSender", e);
    }
  }

  @Override
  public void stopSending() {
    scheduler.shutdown();
    if (socket != null) {
      socket.close();
    }
    logger.info("StupidBeaconSender stopped");
  }
}
