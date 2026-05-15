package com.deges.rest_api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "wago_status") 
public class WagoStatus {

    @Id
    private String id;

    private int rawValue;           // the raw integer received
    private boolean[] lights;       // converted 16-bit binary array
    private LocalDateTime timestamp;
}