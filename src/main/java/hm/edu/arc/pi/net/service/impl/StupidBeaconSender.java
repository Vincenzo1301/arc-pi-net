package hm.edu.arc.pi.net.service.impl;

import static java.net.InetAddress.getByName;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import hm.edu.arc.pi.net.service.BeaconSender;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StupidBeaconSender implements BeaconSender {

  private static final Logger logger = LoggerFactory.getLogger(StupidBeaconSender.class);
  private static final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor();

  private static final String MESSAGE = "Hello from StupidBeaconSender!";

  @Value("${experimental.wifi.address}")
  private String wlanIp;

  @Value("${experimental.wifi.broadcast.address}")
  private String broadcastIp;

  @Value("${experimental.wlan.port}")
  private int port;

  private DatagramSocket socket;

  @Override
  public void startSending() {
    try {
      socket = new DatagramSocket(0, getByName(wlanIp));
      socket.setBroadcast(true);

      scheduler.scheduleAtFixedRate(
          () -> {
            try {
              var data = MESSAGE.getBytes(StandardCharsets.UTF_8);
              var packet = new DatagramPacket(data, data.length, getByName(broadcastIp), port);
              socket.send(packet);
              logger.info("Sent message: {}", MESSAGE);
            } catch (Exception e) {
              logger.error("Error sending packet", e);
            }
          },
          0,
          2,
          SECONDS);
      logger.info("StupidBeaconSender started");
    } catch (Exception e) {
      System.err.println("Error starting StupidBeaconSender: " + e.getMessage());
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
