package hm.edu.arc.pi.net.data;

import com.google.gson.Gson;

public record Beacon(String sourceId, long timestamp) {

  public String toJson() {
    return new Gson().toJson(this);
  }
}
