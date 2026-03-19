package org.example.shareserver.services;

import org.example.shareserver.models.dtos.ItemMarketDTO;
import org.example.shareserver.models.entities.Item;
import org.example.shareserver.models.entities.User;
import org.example.shareserver.repositories.MarketRepository;
import org.example.shareserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MarketService {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MarketRepository marketRepository;
    public ResponseEntity<?> addItemOnMarket(String token, ItemMarketDTO itemMarketDTO) {
        String id = jwtService.getDataFromToken(token.replace("Bearer ", ""));
        itemMarketDTO.setOwnerId(id);
        marketRepository.save(itemMarketDTO);
        Optional<User> user = userRepository.findById(id);
        for (Item i : user.get().getItems()) {
            if (i.getId().equals(itemMarketDTO.getItem().getId())) {
                user.get().getItems().remove(i);
                break;
            }
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> viewMarket() {
        List<ItemMarketDTO> market = marketRepository.findAll();
        market.sort(Comparator.comparingInt(ItemMarketDTO::getPrice));
        return ResponseEntity.ok().body(market);
    }
}
