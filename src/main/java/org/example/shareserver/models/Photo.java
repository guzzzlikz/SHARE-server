package org.example.shareserver.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Photo {
    private String id;
    private String productId;
    private String path;
    private String format;
    private String title;
}
