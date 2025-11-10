package com.cagong.receiptpowerserver.domain.mqtt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class MqttController {

    private final MqttPublisher publisher;
    private final MqttService mqttService;

    @PostMapping("/on")
    public String turnOn() {
        mqttService.turnOn();
        return "Sent ON command";
    }

    @PostMapping("/off")
    public String turnOff() {
        mqttService.turnOff();
        return "Sent OFF command";
    }

    @PostMapping("/time")
    public String setTimer(@RequestParam Long cafeId, int time) {
        String message = mqttService.startTimer(cafeId, time);
        return "Timer set " + message;
    }
}
