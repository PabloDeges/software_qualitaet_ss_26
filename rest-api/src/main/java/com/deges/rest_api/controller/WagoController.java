package com.deges.rest_api.controller;

import com.deges.rest_api.model.WagoStatus;
import com.deges.rest_api.repository.WagoStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wago")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WagoController {

    private final WagoStatusRepository wagoRepo;
    private final MessageChannel mqttOutboundChannel;

    // GET latest status
    @GetMapping("/status/latest")
    public WagoStatus getLatest() {
        return wagoRepo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream()
                .findFirst()
                .orElse(null);
    }

    // POST control command (Task 5)
    @PostMapping("/control")
    public Map<String, String> sendControl(@RequestBody Map<String, Integer> body) {
        int command = body.get("command");

        if (command < 0 || command > 3) {
            return Map.of("status", "error", "message", "Command must be 0-3");
        }

        mqttOutboundChannel.send(
            MessageBuilder
                .withPayload(String.valueOf(command))
                .setHeader("mqtt_topic", "Wago750/Control")
                .build()
        );

        return Map.of("status", "ok", "command", String.valueOf(command));
    }
}