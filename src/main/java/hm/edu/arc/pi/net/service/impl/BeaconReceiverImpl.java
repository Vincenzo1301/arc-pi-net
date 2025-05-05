package hm.edu.arc.pi.net.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hm.edu.arc.pi.net.data.Beacon;
import hm.edu.arc.pi.net.service.BeaconReceiver;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BeaconReceiverImpl implements BeaconReceiver, Runnable {

  private static final Logger logger = LoggerFactory.getLogger(BeaconReceiverImpl.class);

  private static final int PORT = 12345;
  private static final int BUFFER_SIZE = 2048;
  private static final String INTERFACE_NAME = "wlan0";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final List<Beacon> receivedBeacons = new CopyOnWriteArrayList<>();

  private Thread receiverThread;
  private volatile boolean running = false;

  @Override
  public void startReceiving() {
    if (running) {
      logger.warn("BeaconReceiver is already running");
      return;
    }

    running = true;
    receiverThread = new Thread(this, "BeaconReceiverThread");
    receiverThread.start();
    logger.info("BeaconReceiver started");
  }

  @Override
  public void stopReceiving() {
    if (!running) {
      logger.warn("BeaconReceiver is not running");
      return;
    }

    running = false;
    if (receiverThread != null) {
      receiverThread.interrupt();
      try {
        receiverThread.join(1000); // Wait up to 1 second for thread to finish
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      logger.info("BeaconReceiver stopped");
    }
  }

  @Override
  public void run() {
    try (DatagramSocket socket = new DatagramSocket(null)) {
      NetworkInterface wlan = NetworkInterface.getByName(INTERFACE_NAME);
      if (wlan == null) {
        throw new RuntimeException("Interface wlan0 not found");
      }

      InetAddress wlanAddress = wlan.getInetAddresses().nextElement();
      socket.bind(new InetSocketAddress(wlanAddress, PORT));
      logger.info("BeaconReceiver listening on {}:{}", wlanAddress.getHostAddress(), PORT);

      byte[] buffer = new byte[BUFFER_SIZE];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

      while (running && !Thread.currentThread().isInterrupted()) {
        try {
          socket.receive(packet);
          String json = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
          Beacon beacon = objectMapper.readValue(json, Beacon.class);

          receivedBeacons.add(beacon);
          logger.debug("Received Beacon from {} at {}", beacon.nodeId(), beacon.timestamp());
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

  public List<Beacon> getReceivedBeacons() {
    return List.copyOf(receivedBeacons);
  }

  public void clearReceivedBeacons() {
    receivedBeacons.clear();
  }
}
