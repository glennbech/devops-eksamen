package no.eksamen.devops;


import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class demoController {


    private MeterRegistry meterRegistry;

    @Autowired
    public demoController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostMapping(path = "/tx", consumes = "application/json", produces = "application/json")
    public void addMember(@RequestBody Transaction tx) {
        meterRegistry.counter("txcount2", "currency", tx.getCurrency()).increment();
    }
}








//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class demoController {
//
//    @GetMapping(path = "/ping")
//    public String ping() {
//        return "ping";
//    }
//
//    @GetMapping(path = "/pong")
//    public String pong() {
//        return "pong";
//    }
//
//    @GetMapping(path = "/hello")
//    public String hello() {
//        return "Hello World! :D";
//    }
//
//    @GetMapping(path = "/helloworld")
//    public String helloworld() {
//        return "Hello World!! this is me trying to learn devOps ";
//    }
//
//}
