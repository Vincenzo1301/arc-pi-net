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

  private static final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor();

  @Value("${experimental.wifi.broadcast}")
  private String broadcastAddress;

  @Value("${experimental.wifi.host}")
  private String wifiAddress;

  @Value("${experimental.wifi.sender-port}")
  private int port;

  private DatagramSocket socket;

  @Override
  public void startSending() {
    try {
      socket = new DatagramSocket(0, getByName(wifiAddress));
      socket.setBroadcast(true);

      scheduler.scheduleAtFixedRate(this::sendMessage, 0, 2, SECONDS);
    } catch (Exception e) {
      System.err.println("Error starting StupidBeaconSender: " + e.getMessage());
    }
  }

  @Override
  public void stopSending() {
    scheduler.shutdown();
    if (socket != null && !socket.isClosed()) {
      socket.close();
    }
  }

  private void sendMessage() {
    try {
      byte[] data = "Hello from StupidBeaconSender!".getBytes(UTF_8);
      var target = getByName(broadcastAddress);
      var packet = new DatagramPacket(data, data.length, target, port);
      socket.send(packet);
    } catch (Exception e) {
      System.err.println("Error sending packet: " + e.getMessage());
    }
  }
}
