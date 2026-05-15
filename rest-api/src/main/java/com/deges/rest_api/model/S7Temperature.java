package com.deges.rest_api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "s7_temperature")
public class S7Temperature {

    @Id
    private String id;

    private String type;        // "Ist", "Soll", or "Differenz"
    private double value;
    private LocalDateTime timestamp;
}