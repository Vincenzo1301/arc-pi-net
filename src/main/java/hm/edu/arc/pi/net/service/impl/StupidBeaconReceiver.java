package hm.edu.arc.pi.net.service.impl;

import static java.lang.Byte.MAX_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import hm.edu.arc.pi.net.data.Beacon;
import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.BeaconReceiver;
import hm.edu.arc.pi.net.service.LogService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StupidBeaconReceiver implements BeaconReceiver {

  @Value("${experimental.wifi.receive-port}")
  private int port;

  @Value("${experimental.wifi.host}")
  private String sourceId;

  private final LogService logService;

  private DatagramSocket socket;

  private Thread receiverThread;
  private volatile boolean running = false;

  public StupidBeaconReceiver(LogService logService) {
    this.logService = logService;
  }

  @Override
  public void startReceiving() {
    try {
      System.out.println("Starting beacon receiver with. Listening on port: " + port);
      running = true;
      receiverThread = new Thread(this::receive);
      receiverThread.start();
      System.out.println("Started receiver thread");
    } catch (Exception e) {
      System.err.println("Error starting StupidBeaconReceiver: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stopReceiving() {
    System.out.println("Stopping beacon receiver...");
    running = false;
    if (socket != null && !socket.isClosed()) {
      socket.close();
    }
    if (receiverThread != null) {
      receiverThread.interrupt();
    }
    System.out.println("Stopped receiver");
  }

  private void receive() {
    var gson = new Gson();
    try {
      socket = new DatagramSocket(port);
      System.out.println("Listening for beacons on port: " + port);
      while (running) {
        var buf = new byte[MAX_VALUE];
        var packet = new DatagramPacket(buf, buf.length);
        System.out.println("Waiting for packet...");
        socket.receive(packet);
        var json = new String(packet.getData(), 0, packet.getLength(), UTF_8);
        Beacon beacon = gson.fromJson(json, Beacon.class);
        if (beacon != null && !beacon.sourceId().equals(sourceId)) {
          System.out.println("Received beacon: " + beacon);
          logService.addLog(new Log(beacon.toString(), System.currentTimeMillis()));
        }
      }
    } catch (Exception e) {
      if (running) {
        System.err.println("Error in receive(): " + e.getMessage());
        throw new RuntimeException(e);
      }
    } finally {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    }
  }
}
