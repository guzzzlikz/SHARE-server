package org.example.shareserver;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full context test. Disabled by default: requires MongoDB and AI config (e.g. OPENAI_API_KEY).
 * Run with -Dspring.profiles.active=test when DB is available.
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires MongoDB and AiConfig (OPENAI_API_KEY)")
class ShareServerApplicationTests {

    @Test
    void contextLoads() {
    }
}
