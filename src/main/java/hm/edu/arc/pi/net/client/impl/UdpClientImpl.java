package hm.edu.arc.pi.net.client.impl;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.startVirtualThread;
import static java.net.InetAddress.getByName;
import static java.nio.charset.StandardCharsets.UTF_8;

import hm.edu.arc.pi.net.client.UdpClient;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UdpClientImpl implements UdpClient {

  private DatagramSocket socket;
  private boolean isReceiving;
  private Thread receiveThread;

  @Override
  public void initialize(int port) {
    try {
      socket = new DatagramSocket(port);
      socket.setBroadcast(true);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize UDP client", e);
    }
  }

  @Override
  public void startReceiving(Consumer<DatagramPacket> messageHandler) {
    if (isReceiving) {
      return;
    }

    isReceiving = true;
    receiveThread =
        startVirtualThread(
            () -> {
              var receiveBuffer = new byte[1024];
              var receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

              while (isReceiving && !currentThread().isInterrupted()) {
                try {
                  socket.receive(receivePacket);
                  messageHandler.accept(receivePacket);
                } catch (Exception e) {
                  if (isReceiving) {
                    System.err.println("Error receiving UDP message: " + e.getMessage());
                  }
                }
              }
            });
  }

  @Override
  public void stopReceiving() {
    isReceiving = false;
    if (receiveThread != null) {
      receiveThread.interrupt();
      try {
        receiveThread.join(1000);
      } catch (InterruptedException e) {
        currentThread().interrupt();
      }
    }
  }

  @Override
  public void broadcast(DatagramPacket packet) {
    try {
      socket.send(packet);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send UDP broadcast", e);
    }
  }

  @Override
  public void close() {
    stopReceiving();
    if (socket != null && !socket.isClosed()) {
      socket.close();
    }
  }
}
