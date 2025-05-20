package hm.edu.arc.pi.net.service.impl;

import static java.util.Collections.unmodifiableList;

import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.LogService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

  private final List<Log> logs;

  public LogServiceImpl() {
    this.logs = new ArrayList<>();
  }

  @Override
  public void addLog(Log log) {
    logs.add(log);
  }

  @Override
  public List<Log> getLogs() {
    return unmodifiableList(logs);
  }
}
