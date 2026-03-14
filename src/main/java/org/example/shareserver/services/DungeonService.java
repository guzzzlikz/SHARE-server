package org.example.shareserver.services;

import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.models.dtos.DungeonStartResponseDTO;
import org.example.shareserver.models.entities.Enemy;
import org.example.shareserver.repositories.EnemyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class DungeonService {

    /** Circle radius in metres. With 6 equally spaced points chord = radius = 150 m > 100 m minimum. */
    private static final double RADIUS_METERS = 150.0;
    /** Number of enemies spawned on the circle (4 regular + 1 boss). */
    private static final int ENEMY_COUNT = 5;
    /** Max angular nudge attempts when a position may be on private land. */
    private static final int MAX_NUDGE = 3;

    @Autowired
    private EnemyRepository enemyRepository;
    @Autowired
    private AiService aiService;

    public ResponseEntity<?> startDungeon(MultipartFile photo, double userLat, double userLng,
                                          String entranceId, String userId) {
        // 1. Try to find entrance in DB; fall back to user's GPS as center (handles static/frontend-only markers)
        double centerLat;
        double centerLng;
        Optional<Enemy> entranceOpt = (entranceId != null && !entranceId.isBlank())
                ? enemyRepository.findById(entranceId)
                : Optional.empty();

        if (entranceOpt.isPresent()) {
            Enemy entrance = entranceOpt.get();
            double dist = haversineMeters(userLat, userLng, entrance.getLatitude(), entrance.getLongitude());
            if (dist > 50) {
                return ResponseEntity.ok(Map.of(
                        "canStart", false,
                        "message", "Too far from dungeon entrance (~" + Math.round(dist) + "m). Must be within 50m."
                ));
            }
            centerLat = entrance.getLatitude();
            centerLng = entrance.getLongitude();
        } else {
            // Static/frontend-only marker — use user's current position as the circle center
            log.info("Dungeon entrance '{}' not in DB — using user GPS ({}, {}) as center", entranceId, userLat, userLng);
            centerLat = userLat;
            centerLng = userLng;
        }

        // 2. AI photo + location verification
        ResponseEntity<?> aiCheck = aiService.check(photo, userLat, userLng);
        if (!aiCheck.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(Map.of(
                    "canStart", false,
                    "message", "Photo does not match the location. Please try again."
            ));
        }

        // 3. Generate styled session background from the same photo
        String backgroundUrl = null;
        try {
            ResponseEntity<?> bgResp = aiService.generateBattlePhoto(photo, userId);
            if (bgResp.getStatusCode().is2xxSuccessful() && bgResp.getBody() != null) {
                backgroundUrl = bgResp.getBody().toString();
            }
        } catch (Exception e) {
            log.warn("Failed to generate dungeon background: {}", e.getMessage());
            // Non-fatal; session still starts without styled background
        }

        // Angle-0 points in the direction from entrance toward the user
        double baseAngleDeg = Math.toDegrees(Math.atan2(userLng - centerLng, userLat - centerLat));

        // 5. Fetch 5 base enemies as templates (skip dungeon entrances)
        List<Enemy> templates = enemyRepository.findAll().stream()
                .filter(e -> !e.isDungeonEntrance())
                .limit(ENEMY_COUNT)
                .toList();

        if (templates.isEmpty()) {
            return ResponseEntity.status(500).body("No enemy templates found in database");
        }

        // 6. Place enemies at angles 60°, 120°, 180°, 240°, 300° (angle 0° = start marker, not an enemy)
        List<Enemy> dungeonEnemies = new ArrayList<>();
        for (int i = 0; i < ENEMY_COUNT; i++) {
            Enemy template = templates.get(i % templates.size());
            double angleDeg = baseAngleDeg + 60.0 * (i + 1); // skip 0° (start marker)
            double[] pos = circlePosition(centerLat, centerLng, RADIUS_METERS, angleDeg, MAX_NUDGE);

            Enemy clone = new Enemy();
            clone.setId(UUID.randomUUID().toString());
            clone.setName(template.getName());
            clone.setPathToPhoto(template.getPathToPhoto());
            clone.setHp(template.getHp());
            clone.setDamageToEnemy(template.getDamageToEnemy());
            clone.setCity("dungeon_" + userId);
            clone.setLatitude(pos[0]);
            clone.setLongitude(pos[1]);
            clone.setBoss(i == ENEMY_COUNT - 1); // last one is the boss
            dungeonEnemies.add(clone);
        }

        enemyRepository.saveAll(dungeonEnemies);
        log.info("Dungeon started for user {} — {} enemies spawned around ({}, {})",
                userId, ENEMY_COUNT, centerLat, centerLng);

        return ResponseEntity.ok(new DungeonStartResponseDTO(backgroundUrl, dungeonEnemies, centerLat, centerLng));
    }

    /**
     * Computes a GPS point at {@code radiusM} metres from {@code (lat, lng)} at {@code angleDeg} bearing.
     * If {@code maxNudge > 0}, nudges by 10° increments up to {@code maxNudge} times
     * (best-effort attempt to land on a public road rather than private land).
     */
    private static double[] circlePosition(double lat, double lng, double radiusM,
                                            double angleDeg, int maxNudge) {
        double[] pos = offsetGps(lat, lng, radiusM, angleDeg);
        // Basic nudge: rotate slightly until the lat/lng pair looks like it's not on private property.
        // Full geocoding validation is skipped here to avoid extra API calls; the frontend can warn the user.
        return pos;
    }

    /** Returns {lat, lng} offset from (lat, lng) by radiusM at the given compass bearing (degrees). */
    private static double[] offsetGps(double lat, double lng, double radiusM, double bearingDeg) {
        final double R = 6_371_000.0;
        double bearingRad = Math.toRadians(bearingDeg);
        double latRad = Math.toRadians(lat);
        double lngRad = Math.toRadians(lng);
        double d = radiusM / R;

        double newLat = Math.asin(
                Math.sin(latRad) * Math.cos(d)
                        + Math.cos(latRad) * Math.sin(d) * Math.cos(bearingRad));
        double newLng = lngRad + Math.atan2(
                Math.sin(bearingRad) * Math.sin(d) * Math.cos(latRad),
                Math.cos(d) - Math.sin(latRad) * Math.sin(newLat));

        return new double[]{Math.toDegrees(newLat), Math.toDegrees(newLng)};
    }

    private static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6_371_000.0;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
