package com.example.productservice.service;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request){
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        productRepository.save(product);
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    public ProductResponse getProductById(Long id){
        return productRepository.findById(id)
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .build())
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll().stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .build())
                .toList();
    }

    public ProductResponse updateProduct(Long id, ProductRequest request){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        productRepository.save(product);
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    public String deleteProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
        return "Product deleted successfully";
    }

    public String reduceQuantity(Integer quantity, Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if(product.getQuantity() < quantity){
            throw new RuntimeException("Insufficient quantity");
        }
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        return "Quantity reduced successfully";
    }
}
