package com.deges.rest_api.repository;

import com.deges.rest_api.model.S7Temperature;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface S7TemperatureRepository extends MongoRepository<S7Temperature, String> {
}
