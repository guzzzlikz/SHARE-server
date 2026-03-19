package org.example.shareserver.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shareserver.models.entities.Item;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection="market")
public class ItemMarketDTO {
    private String ownerId;
    private Item item;
    private int price;
}
