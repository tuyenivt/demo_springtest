package com.coloza.demo.springtest.web;

import com.coloza.demo.springtest.model.Review;
import com.coloza.demo.springtest.model.ReviewEntry;
import com.coloza.demo.springtest.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @MockitoBean
    private ReviewService service;

    @Autowired
    private MockMvc mockMvc;

    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("GET /review/reviewId - Found")
    void testGetReviewByIdFound() throws Exception {
        // Set up our mocked service
        var mockReviewEntry = new ReviewEntry("test-user", new Date(), "Great product");
        var mockReview = Review.builder().id("reviewId").productId(1).version(1).entries(List.of(mockReviewEntry)).build();
        doReturn(Optional.of(mockReview)).when(service).findById("reviewId");

        // Execute the GET request
        mockMvc.perform(get("/review/{id}", "reviewId"))

                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/reviewId"))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is("reviewId")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries.length()", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("test-user")))
                .andExpect(jsonPath("$.entries[0].review", is("Great product")));
    }

    @Test
    @DisplayName("GET /review/reviewId - Not Found")
    void testGetReviewByIdNotFound() throws Exception {
        // Set up our mocked service
        doReturn(Optional.empty()).when(service).findById("reviewId");

        // Execute the GET request
        mockMvc.perform(get("/review/{id}", "reviewId"))

                // Validate that we get a 404 Not Found response
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /review - Success")
    void testCreateReview() throws Exception {
        // Set up mocked service
        var postReviewEntry = new ReviewEntry("test-user", new Date(), "Great product");
        var postReview = Review.builder().productId(1).entries(List.of(postReviewEntry)).build();

        var mockReviewEntry = new ReviewEntry("test-user", new Date(), "Great product");
        var mockReview = Review.builder().id("reviewId").productId(1).version(1).entries(List.of(mockReviewEntry)).build();

        doReturn(mockReview).when(service).save(any());

        mockMvc.perform(post("/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(postReview)))

                // Validate the response code and content type
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/reviewId"))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is("reviewId")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries.length()", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("test-user")))
                .andExpect(jsonPath("$.entries[0].review", is("Great product")));
    }

    @Test
    @DisplayName("POST /review/{productId}/entry")
    void testAddEntryToReview() throws Exception {
        // Set up mocked service
        var mockReviewEntry = new ReviewEntry("test-user", new Date(), "Great product");
        var mockReview = Review.builder().id("1").productId(1).version(1).build();
        var returnedReview = Review.builder().id("1").productId(1).version(2).entries(List.of(mockReviewEntry)).build();

        // Handle lookup
        doReturn(Optional.of(mockReview)).when(service).findByProductId(1);

        // Handle save
        doReturn(returnedReview).when(service).save(any());

        mockMvc.perform(post("/review/{productId}/entry", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(mockReviewEntry)))

                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/1"))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries.length()", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("test-user")))
                .andExpect(jsonPath("$.entries[0].review", is("Great product")));
    }
}
