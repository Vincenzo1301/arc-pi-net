package hm.edu.arc.pi.net.service.impl;

import static java.net.InetAddress.getByName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.gson.Gson;
import hm.edu.arc.pi.net.data.Beacon;
import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.BeaconSender;
import hm.edu.arc.pi.net.service.LogService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BeaconSenderImpl implements BeaconSender {

  @Value("${experimental.wifi.broadcast}")
  private String broadcastAddress;

  @Value("${experimental.wifi.sender-port}")
  private int senderPort;

  @Value("${experimental.wifi.receive-port}")
  private int receivePort;

  @Value("${experimental.wifi.host}")
  private String sourceId;

  private final LogService logService;

  private ScheduledExecutorService scheduler;
  private DatagramSocket socket;

  public BeaconSenderImpl(LogService logService) {
    this.logService = logService;
  }

  @Override
  public void startSending() {
    try {
      socket = new DatagramSocket(senderPort);
      socket.setBroadcast(true);
      scheduler = newSingleThreadScheduledExecutor();
      scheduler.scheduleAtFixedRate(this::sendMessage, 0, 2, SECONDS);
      System.out.println("Started sending beacons every 2 seconds");
    } catch (Exception e) {
      throw new RuntimeException("Failed to start beacon sender", e);
    }
  }

  public void stopSending() {
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
      var gson = new Gson();
      var currentTimeMillis = System.currentTimeMillis();
      var beacon = new Beacon(sourceId, currentTimeMillis);
      var json = gson.toJson(beacon).getBytes(UTF_8);
      var target = getByName(broadcastAddress);
      var packet = new DatagramPacket(json, json.length, target, receivePort);
      socket.send(packet);
      logService.addLog(new Log(sourceId, currentTimeMillis));
      System.out.println("Sending beacon: " + beacon);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send beacon", e);
    }
  }
}
