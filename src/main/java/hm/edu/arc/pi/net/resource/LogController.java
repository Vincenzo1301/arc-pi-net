package hm.edu.arc.pi.net.resource;

import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.LogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

  private final LogService logService;

  public LogController(LogService logService) {
    this.logService = logService;
  }

  @GetMapping
  public ResponseEntity<List<Log>> getLogs() {
    return ResponseEntity.ok(logService.getLogs());
  }
}
