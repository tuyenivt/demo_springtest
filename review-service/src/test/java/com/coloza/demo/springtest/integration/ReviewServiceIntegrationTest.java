package com.coloza.demo.springtest.integration;

import com.coloza.demo.springtest.model.Review;
import com.coloza.demo.springtest.model.ReviewEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ReviewServiceIntegrationTest {
    @Container
    private static MongoDBContainer mongo = new MongoDBContainer("mongo:8.2");
    private static MongoClient client;
    private static MongoDatabase database;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * MongoSpringExtension method that returns the autowired MongoTemplate to use for MongoDB interactions.
     *
     * @return The autowired MongoTemplate instance.
     */
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void setUpAll() {
        mongo.start();
        client = MongoClients.create(mongo.getReplicaSetUrl());
        database = client.getDatabase("test");
    }

    @BeforeEach
    void loadInitialData() throws Exception {
        database.drop();
        try (var is = getClass().getResourceAsStream("/data/sample.json")) {
            if (is == null) throw new RuntimeException("sample.json not found");
            var documents = mapper.readValue(is, new TypeReference<List<Document>>() {
            });
            database.getCollection("Reviews").insertMany(documents);
        }
    }

    @Test
    @DisplayName("GET /review/1 - Found")
    void testGetReviewByIdFound() throws Exception {

        // Execute the GET request
        mockMvc.perform(get("/review/{id}", 1))

                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/1"))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.entries.length()", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("user1")))
                .andExpect(jsonPath("$.entries[0].review", is("This is a review")));
    }

    @Test
    @DisplayName("GET /review/99 - Not Found")
    void testGetReviewByIdNotFound() throws Exception {

        // Execute the GET request
        mockMvc.perform(get("/review/{id}", 99))

                // Validate that we get a 404 Not Found response
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /review - Success")
    void testCreateReview() throws Exception {
        // Set up mocked service
        var postReviewEntry = ReviewEntry.builder().username("test-user").review("Great product").build();
        var postReview = Review.builder().productId(1).entries(List.of(postReviewEntry)).build();

        mockMvc.perform(post("/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(postReview)))

                // Validate the response code and content type
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().exists(HttpHeaders.LOCATION))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.entries.length()", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("test-user")))
                .andExpect(jsonPath("$.entries[0].review", is("Great product")))
                .andExpect(jsonPath("$.entries[0].date", any(String.class)));
    }

    @Test
    @DisplayName("POST /review/{productId}/entry")
    void testAddEntryToReview() throws Exception {
        // Set up mocked service
        var reviewEntry = ReviewEntry.builder().username("test-user").review("Great product").build();

        mockMvc.perform(post("/review/{productId}/entry", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(reviewEntry)))

                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/1"))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries.length()", is(2)))
                .andExpect(jsonPath("$.entries[0].username", is("user1")))
                .andExpect(jsonPath("$.entries[0].review", is("This is a review")))
                .andExpect(jsonPath("$.entries[1].username", is("test-user")))
                .andExpect(jsonPath("$.entries[1].review", is("Great product")))
                .andExpect(jsonPath("$.entries[1].date", any(String.class)));
    }
}
