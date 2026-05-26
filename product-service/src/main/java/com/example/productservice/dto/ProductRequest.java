package com.example.productservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private Double price;
    private Integer quantity;
}
