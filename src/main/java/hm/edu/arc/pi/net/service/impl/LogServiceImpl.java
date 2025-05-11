package hm.edu.arc.pi.net.service.impl;

import static java.util.Collections.unmodifiableList;

import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.LogService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link LogService} interface that provides in-memory storage of log
 * entries.
 *
 * <p>This service is annotated with {@code @Service} to be recognized as a Spring component.
 */
@Service
public class LogServiceImpl implements LogService {

  private final List<Log> logs;

  /** Constructs a new {@code LogServiceImpl} with an empty list of logs. */
  public LogServiceImpl() {
    this.logs = new ArrayList<>();
  }

  /**
   * Adds a log entry to the in-memory log list.
   *
   * @param log the {@link Log} object to be added; must not be {@code null}
   */
  @Override
  public void addLog(Log log) {
    logs.add(log);
  }

  /**
   * Returns an unmodifiable view of all log entries.
   *
   * @return a {@link List} of {@link Log} objects
   */
  @Override
  public List<Log> getLogs() {
    return unmodifiableList(logs);
  }
}
