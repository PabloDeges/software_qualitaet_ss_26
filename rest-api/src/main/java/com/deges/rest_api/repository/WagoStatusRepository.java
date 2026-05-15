package com.deges.rest_api.repository;

import com.deges.rest_api.model.WagoStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WagoStatusRepository extends MongoRepository<WagoStatus, String> {
}