package com.coloza.demo.springtest.service;

import com.coloza.demo.springtest.model.Product;
import com.coloza.demo.springtest.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
class ProductServiceTest {

    /**
     * The service that we want to test.
     */
    @Autowired
    private ProductService service;

    /**
     * A mock version of the ProductRepository for use in our tests.
     */
    @MockitoBean
    private ProductRepository repository;

    @Test
    @DisplayName("Test findById Success")
    void testFindByIdSuccess() {
        // Set up our mock
        var mockProduct = new Product(1, "Product Name", 10, 1);
        doReturn(Optional.of(mockProduct)).when(repository).findById(1);

        // Execute the service call
        var returnedProduct = service.findById(1);

        // Assert the response
        Assertions.assertTrue(returnedProduct.isPresent(), "Product was not found");
        Assertions.assertSame(returnedProduct.get(), mockProduct, "Products should be the same");
    }

    @Test
    @DisplayName("Test findById Not Found")
    void testFindByIdNotFound() {
        // Set up our mock
        doReturn(Optional.empty()).when(repository).findById(1);

        // Execute the service call
        var returnedProduct = service.findById(1);

        // Assert the response
        Assertions.assertFalse(returnedProduct.isPresent(), "Product was found, when it shouldn't be");
    }

    @Test
    @DisplayName("Test findAll")
    void testFindAll() {
        // Set up our mock
        var mockProduct = new Product(1, "Product Name", 10, 1);
        var mockProduct2 = new Product(2, "Product Name 2", 15, 3);
        doReturn(Arrays.asList(mockProduct, mockProduct2)).when(repository).findAll();

        // Execute the service call
        var products = service.findAll();

        Assertions.assertEquals(2, products.size(), "findAll should return 2 products");
    }

    @Test
    @DisplayName("Test save product")
    void testSave() {
        var mockProduct = Product.builder().id(1).name("Product Name").quantity(10).build();
        doReturn(mockProduct).when(repository).save(any());

        var returnedProduct = service.save(mockProduct);

        Assertions.assertNotNull(returnedProduct, "The saved product should not be null");
        Assertions.assertEquals(1, returnedProduct.getVersion().intValue(),
                "The version for a new product should be 1");
    }
}
