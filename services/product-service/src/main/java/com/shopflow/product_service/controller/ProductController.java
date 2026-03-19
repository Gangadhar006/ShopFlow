package com.shopflow.product_service.controller;

import com.shopflow.product_service.dto.CreateProductRequest;
import com.shopflow.product_service.dto.ProductDto;
import com.shopflow.product_service.dto.UpdateProductRequest;
import com.shopflow.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto create(@Valid @RequestBody CreateProductRequest request) {
        return productService.create(request);
    }

    @GetMapping
    public Page<ProductDto> findAll(@RequestParam(required = false) String category,
                                    @PageableDefault(size = 20, sort = "createdAt",
                                        direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.findAll(category, pageable);
    }

    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable String id) {
        return productService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String id) {
        productService.delete(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto update(@PathVariable String id,
                             @Valid @RequestBody UpdateProductRequest request) {
        return productService.update(id, request);
    }

    // Internal — called by Order Service only
    @PostMapping("/{id}/reserve")
    public void reserveStock(@PathVariable String id,
                             @RequestParam int quantity) {
        productService.reserveStock(id, quantity);
    }
}
