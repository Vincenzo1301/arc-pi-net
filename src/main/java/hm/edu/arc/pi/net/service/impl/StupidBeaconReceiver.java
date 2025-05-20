package hm.edu.arc.pi.net.service.impl;

import static java.net.InetAddress.getByName;
import static java.nio.charset.StandardCharsets.UTF_8;

import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.BeaconReceiver;
import hm.edu.arc.pi.net.service.LogService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StupidBeaconReceiver implements BeaconReceiver {

  @Value("${experimental.wifi.host}")
  private String wifiAddress;

  @Value("${experimental.wifi.receive-port}")
  private int port;

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
      running = true;
      receiverThread = new Thread(this::receive);
      receiverThread.start();
    } catch (Exception e) {
      System.err.println("Error starting StupidBeaconReceiver: " + e.getMessage());
    }
  }

  @Override
  public void stopReceiving() {
    running = false;
    socket.close();
    receiverThread.interrupt();
  }

  private void receive() {
    try (DatagramSocket socket = new DatagramSocket(port, getByName(wifiAddress))) {
      this.socket = socket;

      while (running) {
        var buf = new byte[1024];
        var packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        var message = new String(packet.getData(), 0, packet.getLength(), UTF_8);
        logService.addLog(new Log(message, System.currentTimeMillis()));
      }
    } catch (Exception e) {
      System.err.println("Error in receive(): " + e.getMessage());
    }
  }
}
