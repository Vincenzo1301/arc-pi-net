package hm.edu.arc.pi.net.service.impl;

import static java.lang.System.currentTimeMillis;
import static java.net.InetAddress.getByName;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import hm.edu.arc.pi.net.client.UdpClient;
import hm.edu.arc.pi.net.data.Beacon;
import hm.edu.arc.pi.net.service.BeaconSender;
import java.net.DatagramPacket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BeaconSenderImpl implements BeaconSender {

  @Value("${experimental.wifi.broadcast}")
  private String broadcastAddress;

  @Value("${experimental.wifi.receive-port}")
  private int receivePort;

  @Value("${experimental.wifi.sender-port}")
  private int senderPort;

  @Value("${experimental.wifi.host}")
  private String sourceId;

  private final UdpClient udpClient;
  private final ScheduledTaskExecutor scheduler;

  public BeaconSenderImpl(UdpClient udpClient, ScheduledTaskExecutor scheduler) {
    this.udpClient = udpClient;
    this.scheduler = scheduler;
  }

  @Override
  public void startSending() {
    try {
      udpClient.initialize(senderPort);
      scheduler.startScheduledTask(this::sendBeacon, 0, 2);
      System.out.println("Started sending beacons every 2 seconds");
    } catch (Exception e) {
      throw new RuntimeException("Failed to start beacon sender", e);
    }
  }

  @Override
  public void stopSending() {
    scheduler.stopScheduledTask();
    udpClient.close();
    System.out.println("Stopped sending beacons.");
  }

  private void sendBeacon() {
    try {
      var data = new Beacon(sourceId, currentTimeMillis()).toJson().getBytes(UTF_8);
      udpClient.broadcast(new DatagramPacket(data, data.length, getByName(broadcastAddress), receivePort));
    } catch (Exception e) {
      throw new RuntimeException("Failed to sendBeacon beacon", e);
    }
  }
}
