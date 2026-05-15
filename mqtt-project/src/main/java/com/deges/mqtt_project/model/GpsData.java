package com.deges.mqtt_project.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "gps_data")
public class GpsData {

    @Id
    private String id;

    private double latitude;
    private double longitude;
    private double altitude;
    private Map<String, Object> rawPayload;   // store full JSON just in case
    private LocalDateTime timestamp;
}