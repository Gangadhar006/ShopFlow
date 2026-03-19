package com.shopflow.product_service.service;

import com.shopflow.product_service.document.Product;
import com.shopflow.product_service.dto.CreateProductRequest;
import com.shopflow.product_service.dto.ProductDto;
import com.shopflow.product_service.dto.UpdateProductRequest;
import com.shopflow.product_service.exception.InsufficientStockException;
import com.shopflow.product_service.exception.ProductNotFoundException;
import com.shopflow.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    public ProductDto create(CreateProductRequest request) {
        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .stock(request.stock())
            .category(request.category())
            .images(request.images())
            .build();
        return ProductDto.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(String category, Pageable pageable) {
        if (category != null && !category.isBlank()) {
            return productRepository.findByCategory(category, pageable)
                .map(ProductDto::from);
        }
        return productRepository.findAll(pageable).map(ProductDto::from);
    }

    @Transactional(readOnly = true)
    public ProductDto findById(String id) {
        return productRepository.findById(id)
            .map(ProductDto::from)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public ProductDto update(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        if (request.price() != null) product.setPrice(request.price());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.description() != null) product.setDescription(request.description());

        product.setUpdatedAt(LocalDateTime.now());
        return ProductDto.from(productRepository.save(product));
    }

    public void delete(String id) {
        productRepository.deleteById(id);
    }

    public void reserveStock(String id, int quantity) {
        Query query = new Query(
            Criteria.where("_id").is(id)
                .and("stock").gte(quantity)
        );

        Update update = new Update().inc("stock", -quantity);

        Product result = mongoTemplate.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(false),
            Product.class
        );

        if (result == null) {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
            throw new InsufficientStockException(id, quantity, product.getStock());
        }
    }
}
