package hm.edu.arc.pi.net.data;

public record Beacon(
    int sequenceNumber,
    String sourceId,
    long timestamp,
    int numberOfNeighbours,
    int[] additionalData) {}
