package org.example.shareserver.controllers;

import org.example.shareserver.services.MapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("map")
public class MapsController {
    @Autowired
    private MapsService mapsService;
    @GetMapping("street")
    public ResponseEntity<?> getStreetFromCoordinates(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "en") String lang) {
        return mapsService.getStreetFromCoordinates(latitude, longitude, lang);
    }
    @GetMapping("quiz")
    public ResponseEntity<?> generateQuizOnStreetFromCoordinates(@RequestParam double latitude, @RequestParam double longitude) {
        return mapsService.getQuizOnStreetFromCoordinates(latitude, longitude);
    }
    @GetMapping("city")
    public ResponseEntity<?> getCityFromCoordinates(@RequestParam double latitude, @RequestParam double longitude) {
        return mapsService.getCityFromCoordinates(latitude, longitude);
    }
}
