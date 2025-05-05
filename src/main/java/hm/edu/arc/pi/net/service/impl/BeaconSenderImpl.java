package hm.edu.arc.pi.net.service.impl;

import static java.lang.System.currentTimeMillis;

import com.fasterxml.jackson.databind.ObjectMapper;
import hm.edu.arc.pi.net.data.Beacon;
import hm.edu.arc.pi.net.data.Position;
import hm.edu.arc.pi.net.service.BeaconSender;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BeaconSenderImpl implements BeaconSender {

  private static final Logger logger = LoggerFactory.getLogger(BeaconSenderImpl.class);

  private static final String BROADCAST_IP = "192.168.42.255";
  private static final int PORT = 12345;
  private static final int DELAY = 0;
  private static final String INTERFACE_NAME = "wlan0";

  private final ObjectMapper objectMapper;
  private final DatagramSocket socket;
  private final InetAddress broadcastAddress;
  private final String nodeId;

  private Timer timer;
  private volatile boolean running = false;

  public BeaconSenderImpl() throws Exception {
    this.objectMapper = new ObjectMapper();
    this.socket = new DatagramSocket(null);
    this.socket.setBroadcast(true);

    NetworkInterface wlan = NetworkInterface.getByName(INTERFACE_NAME);
    if (wlan == null) {
      throw new RuntimeException("Interface wlan0 not found");
    }

    InetAddress wlanAddress = wlan.getInetAddresses().nextElement();
    socket.bind(new InetSocketAddress(wlanAddress, 0));
    this.broadcastAddress = InetAddress.getByName(BROADCAST_IP);
    this.nodeId = UUID.randomUUID().toString();
    logger.info(
        "BeaconSender initialized and bound to {}:{}",
        wlanAddress.getHostAddress(),
        socket.getLocalPort());
  }

  private Beacon generateBeacon() {
    return new Beacon(currentTimeMillis(), nodeId, new Position(0, 0), 23.1, 5);
  }

  @Override
  public void startSending() {
    if (running) {
      logger.warn("BeaconSender is already running");
      return;
    }

    running = true;
    timer = new Timer("BeaconSenderTimer");
    timer.scheduleAtFixedRate(
        new TimerTask() {
          public void run() {
            try {
              if (!running) {
                timer.cancel();
                return;
              }

              Beacon beacon = generateBeacon();
              String beaconJson = objectMapper.writeValueAsString(beacon);
              byte[] data = beaconJson.getBytes(StandardCharsets.UTF_8);

              DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, PORT);
              socket.send(packet);

              logger.debug("Beacon sent via wlan0");
            } catch (Exception e) {
              logger.error("Error sending beacon", e);
            }
          }
        },
        DELAY,
        1000); // Send every second
    logger.info("BeaconSender started");
  }

  @Override
  public void stopSending() {
    if (!running) {
      logger.warn("BeaconSender is not running");
      return;
    }

    running = false;
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    logger.info("BeaconSender stopped");
  }
}
