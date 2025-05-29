package hm.edu.arc.pi.net.client;

import java.net.DatagramPacket;
import java.util.function.Consumer;

public interface UdpClient {

  void initialize(int port);

  void startReceiving(Consumer<DatagramPacket> packetConsumer);

  void stopReceiving();

  void broadcast(DatagramPacket packet);

  void close();
}
