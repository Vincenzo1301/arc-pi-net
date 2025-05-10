package hm.edu.arc.pi.net.service.impl;

import hm.edu.arc.pi.net.service.MessageSizeService;
import org.springframework.stereotype.Service;

@Service
public class MessageSizeServiceImpl implements MessageSizeService {

  private double averageMessageSize = 0.0;

  @Override
  public int calculateMessageSize(
      int numPackets, int headerSize, int numMeasurements, int measurementSize) {
    // S = Nb(H) + M'b(m)
    // return (numPackets * headerSize) + (numMeasurements * measurementSize);
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void calculateAverageMessageSize(int newSize) {
    final double alpha = 0.1;
    this.averageMessageSize = alpha * newSize + (1 - alpha) * this.averageMessageSize;
  }

  @Override
  public double getAverageMessageSize() {
    // return this.averageMessageSize;
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public int calculateRequiredBeacons(
      int numMeasurements, int measurementSize, int maxPacketSize, int headerSize) {
    // int payloadPerPacket = maxPacketSize - headerSize;
    // int totalPayload = numMeasurements * measurementSize;
    // return (int) Math.ceil((double) totalPayload / payloadPerPacket);
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
