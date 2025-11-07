package com.coloza.demo.springtest.service;

import com.coloza.demo.springtest.model.Review;
import com.coloza.demo.springtest.model.ReviewEntry;
import com.coloza.demo.springtest.repository.ReviewRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
class ReviewServiceTest {
    /**
     * The service that we want to test.
     */
    @Autowired
    private ReviewService service;

    /**
     * A mock version of the ReviewRepository for use in our tests.
     */
    @MockitoBean
    private ReviewRepository repository;

    @Test
    @DisplayName("Test findById Success")
    void testFindByIdSuccess() {
        // Set up our mock
        var mockReview = Review.builder().id("reviewId").productId(1).version(1).build();
        var now = new Date();
        mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        doReturn(Optional.of(mockReview)).when(repository).findById("reviewId");

        // Execute the service call
        var returnedReview = service.findById("reviewId");

        // Assert the response
        Assertions.assertTrue(returnedReview.isPresent(), "Review was not found");
        Assertions.assertSame(returnedReview.get(), mockReview, "Review should be the same");
    }

    @Test
    @DisplayName("Test findById Not Found")
    void testFindByIdNotFound() {
        // Set up our mock
        doReturn(Optional.empty()).when(repository).findById("1");

        // Execute the service call
        var returnedReview = service.findById("1");

        // Assert the response
        Assertions.assertFalse(returnedReview.isPresent(), "Review was found, when it shouldn't be");
    }

    @Test
    @DisplayName("Test findAll")
    void testFindAll() {
        // Set up our mock
        var mockReview = Review.builder().id("reviewId").productId(1).version(1).build();
        var mockReview2 = Review.builder().id("reviewId2").productId(2).version(1).build();
        doReturn(Arrays.asList(mockReview, mockReview2)).when(repository).findAll();

        // Execute the service call
        var reviews = service.findAll();

        Assertions.assertEquals(2, reviews.size(), "findAll should return 2 reviews");
    }

    @Test
    @DisplayName("Test save review")
    void testSave() {
        var mockReview = Review.builder().id("reviewId").productId(1).version(1).build();
        doReturn(mockReview).when(repository).save(any());

        var returnedReview = service.save(mockReview);

        Assertions.assertNotNull(returnedReview, "The saved review should not be null");
        Assertions.assertEquals(1, returnedReview.getVersion().intValue(),
                "The version for a new review should be 1");
    }
}
