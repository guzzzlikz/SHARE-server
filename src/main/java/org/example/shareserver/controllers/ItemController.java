package org.example.shareserver.controllers;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createEnemy(@RequestBody Item item) {
        itemRepository.save(item);
        return ResponseEntity.ok("Created");
    }
}
