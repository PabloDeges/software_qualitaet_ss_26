package com.deges.mqtt_project.repository;

import com.deges.mqtt_project.model.WagoStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WagoStatusRepository extends MongoRepository<WagoStatus, String> {
}