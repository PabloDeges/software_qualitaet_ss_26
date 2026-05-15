package com.deges.mqtt_project.service;

import com.deges.mqtt_project.model.GpsData;
import com.deges.mqtt_project.model.S7Temperature;
import com.deges.mqtt_project.model.WagoStatus;
import com.deges.mqtt_project.repository.GpsDataRepository;
import com.deges.mqtt_project.repository.S7TemperatureRepository;
import com.deges.mqtt_project.repository.WagoStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandlerService {

    private final WagoStatusRepository wagoRepo;
    private final S7TemperatureRepository s7Repo;
    private final GpsDataRepository gpsRepo;

    public void handleMessage(Message<?> message) {
        // Get the topic so we know how to parse the payload
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = message.getPayload().toString();

        log.info("Received on [{}]: {}", topic, payload);

        try {
            if (topic == null) return;

            if (topic.equals("Wago750/Status") || topic.equals("Random/Integer")) {
                handleWago(payload);

            } else if (topic.startsWith("S7 1500/Temperatur/")) {
                String type = topic.replace("S7 1500/Temperatur/", ""); // "Ist", "Soll", or "Differenz"
                handleTemperature(type, payload);

            } 
            // else if (topic.contains("GNSS")) {
            //     handleGps(payload);
            // }

        } catch (Exception e) {
            log.error("Failed to process message on topic {}: {}", topic, e.getMessage());
        }
    }

    private void handleWago(String payload) {
        // Strip surrounding brackets if present e.g. "[2048]" → "2048"
        String cleaned = payload.trim().replaceAll("[\\[\\]]", "");
        int rawValue = Integer.parseInt(cleaned);
    
        boolean[] lights = new boolean[16];
        for (int i = 0; i < 16; i++) {
            lights[i] = ((rawValue >> i) & 1) == 1;
        }
    
        WagoStatus status = new WagoStatus();
        status.setRawValue(rawValue);
        status.setLights(lights);
        status.setTimestamp(LocalDateTime.now());
    
        wagoRepo.save(status);
        log.info("Saved Wago status: raw={}, lights={}", rawValue, lights);
    }

    private void handleTemperature(String type, String payload) {
        double value = Double.parseDouble(payload.trim());

        S7Temperature temp = new S7Temperature();
        temp.setType(type);
        temp.setValue(value);
        temp.setTimestamp(LocalDateTime.now());

        s7Repo.save(temp);
        log.info("Saved S7 temperature [{}]: {}", type, value);
    }

    // @SuppressWarnings("unchecked")
    // private void handleGps(String payload) throws Exception {
    //     Map<String, Object> raw = objectMapper.readValue(payload, Map.class);

    //     GpsData gps = new GpsData();
    //     gps.setLatitude(toDouble(raw.get("Lat")));
    //     gps.setLongitude(toDouble(raw.get("Lon")));
    //     gps.setAltitude(toDouble(raw.get("Alt")));
    //     gps.setRawPayload(raw);
    //     gps.setTimestamp(LocalDateTime.now());

    //     gpsRepo.save(gps);
    //     log.info("Saved GPS: lat={}, lon={}, alt={}", gps.getLatitude(), gps.getLongitude(), gps.getAltitude());
    // }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        return Double.parseDouble(val.toString());
    }
}