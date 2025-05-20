package hm.edu.arc.pi.net.service;

import hm.edu.arc.pi.net.data.Log;
import java.util.List;

public interface LogService {

  void addLog(Log message);

  List<Log> getLogs();
}
