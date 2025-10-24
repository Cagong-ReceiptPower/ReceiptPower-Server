package com.cagong.receiptpowerserver.domain.mqtt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("!test")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class MqttController {

    private final MqttPublisher publisher;
    private final MqttService mqttService;

    @PostMapping("/on")
    public String turnOn() {
        publisher.publishCommand("mycafe/relay/control", "on");
        return "Sent ON command";
    }

    @PostMapping("/off")
    public String turnOff() {
        publisher.publishCommand("mycafe/relay/control", "off");
        return "Sent OFF command";
    }

    @PostMapping("/time")
    public String setTimer(@RequestParam Long cafeId, int time) {
        String message = mqttService.getTimerMessage(cafeId, time);
        publisher.publishCommand("mycafe/relay/control", message);
        return "Timer set";
    }
}
