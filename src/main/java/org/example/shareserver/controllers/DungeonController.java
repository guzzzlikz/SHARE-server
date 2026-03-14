package org.example.shareserver.controllers;

import org.example.shareserver.components.AuthHeaderHelper;
import org.example.shareserver.services.DungeonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/dungeon")
public class DungeonController {

    @Autowired
    private DungeonService dungeonService;
    @Autowired
    private AuthHeaderHelper authHeaderHelper;

    /**
     * Starts a dungeon session.
     *
     * Multipart body:
     *   - file       : scan photo (for AI location verify + background generation)
     * Query params:
     *   - lat        : user's GPS latitude
     *   - lng        : user's GPS longitude
     *   - entranceId : Enemy id of the dungeon entrance marker
     */
    @PostMapping("/start")
    public ResponseEntity<?> start(
            @RequestPart("file") MultipartFile file,
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam("entranceId") String entranceId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Optional<String> userId = authHeaderHelper.getUserIdFromAuthHeader(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return dungeonService.startDungeon(file, lat, lng, entranceId, userId.get());
    }
}
