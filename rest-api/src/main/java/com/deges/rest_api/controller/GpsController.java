package com.deges.rest_api.controller;

import com.deges.rest_api.model.GpsData;
import com.deges.rest_api.repository.GpsDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gps")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GpsController {

    private final GpsDataRepository gpsRepo;

    @GetMapping("/latest")
    public GpsData getLatest() {
        return gpsRepo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream()
                .findFirst()
                .orElse(null);
    }
}