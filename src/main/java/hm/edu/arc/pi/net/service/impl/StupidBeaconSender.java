package hm.edu.arc.pi.net.service.impl;

import static java.net.InetAddress.getByName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import hm.edu.arc.pi.net.service.BeaconSender;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StupidBeaconSender implements BeaconSender {

  @Value("${experimental.wifi.broadcast}")
  private String broadcastAddress;

  @Value("${experimental.wifi.host}")
  private String wifiAddress;

  @Value("${experimental.wifi.sender-port}")
  private int senderPort;

  @Value("${experimental.wifi.receive-port}")
  private int receivePort;

  private ScheduledExecutorService scheduler;
  private DatagramSocket socket;

  @Override
  public void startSending() {
    try {
      System.out.println("Starting beacon sender with:");
      System.out.println("Broadcast address: " + broadcastAddress);
      System.out.println("Wifi address: " + wifiAddress);
      System.out.println("Sender port: " + senderPort);
      System.out.println("Receive port: " + receivePort);

      socket = new DatagramSocket(senderPort, getByName(wifiAddress));
      socket.setBroadcast(true);
      System.out.println("Created socket on port: " + socket.getLocalPort());

      scheduler = newSingleThreadScheduledExecutor();
      scheduler.scheduleAtFixedRate(this::sendMessage, 0, 2, SECONDS);
      System.out.println("Started sending beacons every 2 seconds");
    } catch (Exception e) {
      System.err.println("Error starting StupidBeaconSender: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public void stopSending() {
    System.out.println("Stopping beacon sender...");

    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, SECONDS)) {
        System.err.println("Scheduler did not terminate in time. Forcing shutdown...");
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      System.err.println("Interrupted while waiting for scheduler to terminate.");
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }

    if (socket != null && !socket.isClosed()) {
      socket.close();
    }

    System.out.println("Stopped sending beacons.");
  }

  private void sendMessage() {
    try {
      var data = "Hello from StupidBeaconSender!".getBytes(UTF_8);
      var target = getByName(broadcastAddress);
      var packet = new DatagramPacket(data, data.length, target, receivePort);
      socket.send(packet);
      System.out.println("Sent packet to " + target + ":" + receivePort);
    } catch (Exception e) {
      System.err.println("Error sending packet: " + e.getMessage());
      throw new RuntimeException("Failed to send beacon", e);
    }
  }
}
