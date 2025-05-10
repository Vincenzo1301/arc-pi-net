package hm.edu.arc.pi.net.service.impl;

import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.BeaconReceiver;
import hm.edu.arc.pi.net.service.LogService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * StupidBeaconReceiver is a simple implementation of the BeaconReceiver interface that listens for
 * UDP packets on a specified port and logs the received messages.
 *
 * <p>This class is not intended for real experiments of the ARC-DSA algorithm and is primarily to
 * test whether the network stack is working correctly.
 */
@Service
public class StupidBeaconReceiver implements BeaconReceiver {

  private static final Logger logger = LoggerFactory.getLogger(StupidBeaconReceiver.class);

  private static final int PORT = 12345;
  private static final int BUFFER_SIZE = 1024;

  private final LogService logService;

  private Thread receiverThread;
  private volatile boolean running = false;

  private DatagramSocket socket;

  public StupidBeaconReceiver(LogService logService) {
    this.logService = logService;
  }

  @Override
  public void startReceiving() {
    try {
      socket = new DatagramSocket(PORT);
      running = true;

      receiverThread =
          new Thread(
              () -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (running) {
                  try {
                    socket.receive(packet);
                    String message =
                        new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    logService.addLog(new Log(message, System.currentTimeMillis()));

                    logger.info("Received message from {}: {}", packet.getAddress(), message);
                  } catch (Exception e) {
                    if (running) {
                      logger.error("Error receiving packet", e);
                    }
                  }
                }
              });

      receiverThread.start();
      logger.info("StupidBeaconReceiver started on port {}", PORT);
    } catch (Exception e) {
      logger.error("Error starting StupidBeaconReceiver", e);
    }
  }

  @Override
  public void stopReceiving() {
    running = false;
    if (socket != null) {
      socket.close();
    }
    if (receiverThread != null) {
      receiverThread.interrupt();
    }
    logger.info("StupidBeaconReceiver stopped");
  }
}
