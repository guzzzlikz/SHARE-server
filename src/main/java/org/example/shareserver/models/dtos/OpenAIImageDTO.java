package org.example.shareserver.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenAIImageDTO {
    private long created;
    private List<ImageData> data;
    public static class ImageData {

        private String b64_json;

        public String getB64_json() {
            return b64_json;
        }

        public void setB64_json(String b64_json) {
            this.b64_json = b64_json;
        }
    }
}
