package hm.edu.arc.pi.net.data;

/**
 * Represents the geographical position of a node.
 *
 * @param latitude Latitude in decimal degrees (e.g., 48.1351).
 * @param longitude Longitude in decimal degrees (e.g., 11.5820).
 */
public record Position(double latitude, double longitude) {}
