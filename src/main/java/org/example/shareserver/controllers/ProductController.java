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
    @AllArgsConstructor
    public static class ParamsDTO {
        private String title;
        private String description;
        private double price;
        private double longitude;
        private double latitude;
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
    public ResponseEntity<?> changeParams(@RequestBody ParamsDTO params,
                                          @RequestParam String productId) {
        return productService.changeParams(params, productId);
    }
}
