package com.deges.rest_api.controller;

import com.deges.rest_api.model.S7Temperature;
import com.deges.rest_api.repository.S7TemperatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/temperature")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TemperatureController {

    private final S7TemperatureRepository s7Repo;

    // GET latest value per type
    @GetMapping("/{type}/latest")
    public S7Temperature getLatest(@PathVariable String type) {
        return s7Repo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream()
                .filter(t -> t.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }

    // GET all values per type
    @GetMapping("/{type}/all")
    public List<S7Temperature> getAll(@PathVariable String type) {
        return s7Repo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream()
                .filter(t -> t.getType().equalsIgnoreCase(type))
                .toList();
    }
}