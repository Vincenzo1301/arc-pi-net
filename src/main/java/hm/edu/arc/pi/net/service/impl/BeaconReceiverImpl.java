package hm.edu.arc.pi.net.service.impl;

import com.google.gson.Gson;
import hm.edu.arc.pi.net.client.UdpClient;
import hm.edu.arc.pi.net.data.Beacon;
import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.BeaconReceiver;
import hm.edu.arc.pi.net.service.LogService;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class BeaconReceiverImpl implements BeaconReceiver {

  @Value("${experimental.wifi.receive-port}")
  private int port;

  @Value("${experimental.wifi.host}")
  private String sourceId;

  private final LogService logService;
  private final UdpClient udpClient;

  public BeaconReceiverImpl(LogService logService, UdpClient udpClient) {
    this.logService = logService;
    this.udpClient = udpClient;
  }

  @Override
  public void startReceiving() {
    try {
      System.out.println("Starting beacon receiver. Listening on port: " + port);
      udpClient.initialize(port);
      udpClient.startReceiving(this::handleReceivedBeacon);
      System.out.println("Started receiver");
    } catch (Exception e) {
      System.err.println("Error starting StupidBeaconReceiver: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stopReceiving() {
    System.out.println("Stopping beacon receiver...");
    udpClient.close();
    System.out.println("Stopped receiver");
  }

  private void handleReceivedBeacon(DatagramPacket packet) {
    try {
      var json = new String(packet.getData(), 0, packet.getLength(), UTF_8);
      var beacon = new Gson().fromJson(json, Beacon.class);
      if (beacon != null && !beacon.sourceId().equals(sourceId)) {
        logService.addLog(new Log(beacon.toString(), currentTimeMillis()));
        System.out.println("Received beacon: " + beacon);
      }
    } catch (Exception e) {
      System.err.println("Error handling beacon message: " + e.getMessage());
    }
  }
}
