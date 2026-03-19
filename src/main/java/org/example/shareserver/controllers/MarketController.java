package org.example.shareserver.controllers;

import org.example.shareserver.models.dtos.ItemMarketDTO;
import org.example.shareserver.services.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("market")
public class MarketController {
    @Autowired
    private MarketService marketService;
    @PostMapping("add")
    public ResponseEntity<?> addItemOnMarket(@RequestHeader("Authorization") String token,
                                             @RequestBody ItemMarketDTO item) {
        return marketService.addItemOnMarket(token, item);
    }
    @GetMapping("view")
    public ResponseEntity<?> viewMarket() {
        return marketService.viewMarket();
    }
}
