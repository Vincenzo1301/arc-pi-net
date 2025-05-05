package hm.edu.arc.pi.net.data;

/**
 * Represents a beacon packet sent by a node in a distributed sensing network.
 *
 * <p>A beacon contains metadata and optional sensor data, which are broadcasted periodically to
 * support distributed estimation, synchronization and network awareness.
 *
 * @param timestamp The Unix timestamp (milliseconds) indicating when the packet was sent.
 * @param nodeId The unique identifier of the node that sent the beacon.
 * @param position The geographical position of the node (nullable if unknown).
 * @param temperature The temperature measured by the node in degrees Celsius.
 * @param neighborCount The number of currently visible neighboring nodes.
 */
public record Beacon(
    long timestamp, String nodeId, Position position, double temperature, int neighborCount) {}
