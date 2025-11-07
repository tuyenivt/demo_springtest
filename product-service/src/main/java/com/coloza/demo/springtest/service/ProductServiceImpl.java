package com.coloza.demo.springtest.service;

import com.coloza.demo.springtest.model.Product;
import com.coloza.demo.springtest.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Optional<Product> findById(Integer id) {
        log.info("Find product with id: {}", id);
        return productRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        log.info("Find all products");
        return productRepository.findAll();
    }

    @Override
    public boolean update(Product product) {
        log.info("Update product: {}", product);
        return productRepository.update(product);
    }

    @Override
    public Product save(Product product) {
        // Set the product version to 1 as we're adding a new product to the database
        product.setVersion(1);

        log.info("Save product to the database: {}", product);
        return productRepository.save(product);
    }

    @Override
    public boolean delete(Integer id) {
        log.info("Delete product with id: {}", id);
        return productRepository.delete(id);
    }
}
