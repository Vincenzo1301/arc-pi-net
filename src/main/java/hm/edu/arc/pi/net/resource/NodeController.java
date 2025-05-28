package hm.edu.arc.pi.net.resource;

import hm.edu.arc.pi.net.service.BeaconReceiver;
import hm.edu.arc.pi.net.service.BeaconSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

  private final BeaconSender beaconSender;
  private final BeaconReceiver beaconReceiver;

  public NodeController(BeaconSender beaconSender, BeaconReceiver beaconReceiver) {
    this.beaconSender = beaconSender;
    this.beaconReceiver = beaconReceiver;
  }

  @PostMapping("/start")
  public ResponseEntity<Void> start() {
    beaconReceiver.startReceiving();
    beaconSender.startSending();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/stop")
  public ResponseEntity<Void> stop() {
    beaconSender.stopSending();
    beaconReceiver.stopReceiving();
    return ResponseEntity.ok().build();
  }
}
