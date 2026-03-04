package org.example.shareserver.services;

import lombok.extern.slf4j.Slf4j;
import org.example.shareserver.controllers.ProductController;
import org.example.shareserver.models.Product;
import org.example.shareserver.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductService {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private ProductRepository productRepository;
    public ResponseEntity<?> addProduct(Product product, String authToken) {
        StringBuilder errorMsg = new StringBuilder();
        if (authToken == null || authToken.isEmpty() || !authToken.startsWith("Bearer ")) {
            log.warn("Possible XSS attack!");
            return ResponseEntity.status(403).body("Token not found");
        }
        String token = authToken.replace("Bearer ", "");
        String ownerId = jwtService.getDataFromToken(token);
        product.setOwnerId(ownerId);
        if (product.getId() == null || product.getId().isEmpty()) {
            log.info("ProductService has been called but id is empty");
            errorMsg.append("Product id is required\n");
        }
        if (product.getTitle() == null || product.getTitle().isEmpty()) {
            log.info("ProductService has been called but title is empty");
            errorMsg.append("Product title is required\n");
        }
        if (product.getDescription() == null || product.getDescription().isEmpty()) {
            log.info("ProductService has been called but description is empty");
            errorMsg.append("Product description is required\n");
        }
        if (product.getPrice() < 0) {
            log.info("ProductService has been called but price is invalid");
            errorMsg.append("Product price is invalid\n");
        }
        if (!errorMsg.toString().isEmpty()) {
            return ResponseEntity.status(400).body(errorMsg.toString());
        }
        //TODO image url
        productRepository.save(product);
        return ResponseEntity.status(200).body("Product added");
    }

    public ResponseEntity<?> removeProduct(String productId) {
        StringBuilder errorMsg = new StringBuilder();
        if (productId == null || productId.isEmpty()) {
            log.info("ProductService has been called but id is empty");
            errorMsg.append("Product id is required\n");
            return ResponseEntity.status(400).body(errorMsg.toString());
        }
        if (!productRepository.existsById(productId)) {
            log.info("ProductService has been called but product not found");
            errorMsg.append("Product not found\n");
        }
        if (!errorMsg.toString().isEmpty()) {
            return ResponseEntity.status(400).body(errorMsg.toString());
        }
        productRepository.deleteById(productId);
        return ResponseEntity.status(200).body("Product removed");
    }

    public ResponseEntity<?> changeParams(ProductController.ParamsDTO params, String productId) {
        StringBuilder errorMsg = new StringBuilder();
        Product mongoProduct = productRepository.findById(productId).orElse(null);
        if (mongoProduct == null) {
            log.info("ProductService has been called but product not found");
            return ResponseEntity.status(404).body("Product not found");
        }
        if (params.getTitle().isEmpty()) {
            log.info("ProductService has been called but title is empty");
            errorMsg.append("Product title is required\n");
        }
        if (params.getTitle() != null && !params.getTitle().isEmpty()) {
            mongoProduct.setTitle(params.getTitle());
        }
        if (params.getDescription().isEmpty()) {
            log.info("ProductService has been called but description is empty");
            errorMsg.append("Product description is required\n");
        }
        if (params.getDescription() != null && !params.getDescription().isEmpty()) {
            mongoProduct.setDescription(params.getDescription());
        }
        if (params.getPrice() < 0) {
            errorMsg.append("Price is invalid\n");
        } else {
            params.setPrice(mongoProduct.getPrice());
        }
        if (!errorMsg.toString().isEmpty()) {
            return ResponseEntity.status(400).body(errorMsg.toString());
        }
        productRepository.save(mongoProduct);
        return ResponseEntity.status(200).body("Product updated");
    }
}
