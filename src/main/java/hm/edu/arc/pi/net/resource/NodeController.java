package hm.edu.arc.pi.net.resource;

import hm.edu.arc.pi.net.data.Log;
import hm.edu.arc.pi.net.service.BeaconReceiver;
import hm.edu.arc.pi.net.service.BeaconSender;
import hm.edu.arc.pi.net.service.LogService;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

  private final LogService logService;
  private final BeaconSender beaconSender;
  private final BeaconReceiver beaconReceiver;

  public NodeController(
      @Qualifier("stupidBeaconSender") BeaconSender beaconSender,
      @Qualifier("stupidBeaconReceiver") BeaconReceiver beaconReceiver,
      LogService logService) {
    this.beaconSender = beaconSender;
    this.beaconReceiver = beaconReceiver;
    this.logService = logService;
  }

  @PostMapping("/start")
  public ResponseEntity<Void> start() {
    beaconReceiver.startReceiving();
    beaconSender.startSending();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/stop")
  public ResponseEntity<Void> stop() {
    beaconReceiver.stopReceiving();
    beaconSender.stopSending();
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<List<Log>> getNodes() {
    return ResponseEntity.ok(logService.getLogs());
  }
}
