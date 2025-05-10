package hm.edu.arc.pi.net.service;

public interface MessageSizeService {

  int calculateMessageSize(
      int numPackets, int headerSize, int numMeasurements, int measurementSize);

  void calculateAverageMessageSize(int newSize);

  double getAverageMessageSize();

  int calculateRequiredBeacons(
          int numMeasurements,
          int measurementSize,
          int maxPacketSize,
          int headerSize
  );
}
