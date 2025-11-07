package com.coloza.demo.springtest.repository;

import com.coloza.demo.springtest.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ProductRepositoryImpl(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;

        // Build a SimpleJdbcInsert object from the specified data source
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("products")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Product> findById(Integer id) {
        try {
            var product = jdbcTemplate.queryForObject("SELECT * FROM products WHERE id = ?",
                    new Object[]{id},
                    (rs, rowNum) -> Product.builder()
                            .id(rs.getInt("id"))
                            .name(rs.getString("name"))
                            .quantity(rs.getInt("quantity"))
                            .version(rs.getInt("version"))
                            .build());
            return Optional.ofNullable(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        return jdbcTemplate.query("SELECT * FROM products",
                (rs, rowNumber) -> Product.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .quantity(rs.getInt("quantity"))
                        .version(rs.getInt("version"))
                        .build());
    }

    @Override
    public boolean update(Product product) {
        return jdbcTemplate.update("UPDATE products SET name = ?, quantity = ?, version = ? WHERE id = ?",
                product.getName(),
                product.getQuantity(),
                product.getVersion(),
                product.getId()) == 1;
    }

    @Override
    public Product save(Product product) {
        // Build the product parameters we want to save
        var parameters = Map.of(
                "name", product.getName(),
                "quantity", product.getQuantity(),
                "version", product.getVersion()
        );

        // Execute the query and get the generated key
        var newId = simpleJdbcInsert.executeAndReturnKey(parameters);

        log.info("Inserting product into database, generated key is: {}", newId);

        // Update the product's ID with the new key
        product.setId((Integer) newId);

        // Return the complete product
        return product;
    }

    @Override
    public boolean delete(Integer id) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ?", id) == 1;
    }
}
