package org.example.shareserver.controllers;

import lombok.*;
import org.example.shareserver.models.Product;
import org.example.shareserver.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("products")
public class ProductController {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ParamsDTO {
        private String title;
        private String description;
        private double price;
    }
    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Product product, @RequestHeader("Authorization") String authHeader) {
        return productService.addProduct(product, authHeader);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeProduct(@RequestParam String productId) {
        return productService.removeProduct(productId);
    }
    @PutMapping("/change")
    public ResponseEntity<?> changeParams(@RequestParam(required = false) double price,
                                          @RequestParam(required = false) String title,
                                          @RequestParam(required = false) String description,
                                          @RequestParam String productId) {
        ParamsDTO params = new ParamsDTO();
        params.price = price;
        if (title != null) {
            params.title = title;
        }
        if (description != null) {
            params.description = description;
        }
        return productService.changeParams(params, productId);
    }
}
