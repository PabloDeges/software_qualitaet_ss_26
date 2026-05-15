package com.deges.mqtt_project.repository;


import com.deges.mqtt_project.model.GpsData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GpsDataRepository extends MongoRepository<GpsData, String> {
}