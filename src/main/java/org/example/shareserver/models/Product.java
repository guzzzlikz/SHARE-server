package org.example.shareserver.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection="products")
public class Product {
    @Id
    private String id;
    private String ownerId;
    private String title;
    private String description;
    private String imageUrl;
    private double price;
}
