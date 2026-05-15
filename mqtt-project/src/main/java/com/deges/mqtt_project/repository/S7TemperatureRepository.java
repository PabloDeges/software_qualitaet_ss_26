package com.deges.mqtt_project.repository;

import com.deges.mqtt_project.model.S7Temperature;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface S7TemperatureRepository extends MongoRepository<S7Temperature, String> {
}
