package hm.edu.arc.pi.net.service;

import hm.edu.arc.pi.net.data.Log;
import java.util.List;

/**
 * Service interface for handling log entries within the system.
 *
 * <p>Provides methods to add new logs and retrieve existing ones.
 */
public interface LogService {

  /**
   * Adds a log entry to the underlying log storage.
   *
   * @param message the {@link Log} object to be added; must not be {@code null}
   */
  void addLog(Log message);

  /**
   * Retrieves all stored log entries.
   *
   * @return a {@link List} of {@link Log} objects representing the stored logs
   */
  List<Log> getLogs();
}
