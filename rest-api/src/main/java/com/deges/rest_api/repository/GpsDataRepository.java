package com.deges.rest_api.repository;


import com.deges.rest_api.model.GpsData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GpsDataRepository extends MongoRepository<GpsData, String> {
}