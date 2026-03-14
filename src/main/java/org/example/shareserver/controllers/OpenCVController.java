package org.example.shareserver.controllers;

import org.example.shareserver.models.dtos.EnemyDTO;
import org.example.shareserver.services.OpenCVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("opencv")
public class OpenCVController {
    @Autowired
    private OpenCVService openCVService;
    @PostMapping("check")
    public ResponseEntity<?> check(@RequestBody EnemyDTO dto) {
        return openCVService.fetchStreetViewImage(dto.getLatitude(), dto.getLongitude(), dto.getId());
    }
}
