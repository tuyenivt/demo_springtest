package com.coloza.demo.springtest.repository;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;
import java.util.List;

@Testcontainers
@SpringBootTest
class ReviewRepositoryMoreElegantTest {
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
    private MongoTemplate mongoTemplate;

    @Autowired
    private ReviewRepository repository;

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
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
        try (var is = getClass().getResourceAsStream("/data/sample6.json")) {
            if (is == null) throw new RuntimeException("sample6.json not found");
            var documents = mapper.readValue(is, new TypeReference<List<Document>>() {
            });
            database.getCollection("Reviews").insertMany(documents);
        }
    }

    @Test
    void testSave() {
        // Create a test Review
        var reviewEntry = new ReviewEntry("test-user", new Date(), "This is a review");
        var review = Review.builder().productId(10).version(1).entries(List.of(reviewEntry)).build();

        // Persist the review to MongoDB
        var savedReview = repository.save(review);

        // Retrieve the review
        var loadedReview = repository.findById(savedReview.getId());

        // Validations
        Assertions.assertTrue(loadedReview.isPresent());
        loadedReview.ifPresent(r -> {
            Assertions.assertEquals(10, r.getProductId().intValue());
            Assertions.assertEquals(1, r.getVersion().intValue(), "Review version should be 1");
            Assertions.assertEquals(1, r.getEntries().size(), "Review 1 should have one entry");
        });
    }

    @Test
    void testFindAll() {
        var reviews = repository.findAll();
        Assertions.assertEquals(6, reviews.size(), "Should be six reviews in the database");
        reviews.forEach(System.out::println);
    }

    @Test
    void testFindByIdSuccess() {
        var review = repository.findById("1");
        Assertions.assertTrue(review.isPresent(), "We should have found a review with ID 1");
        review.ifPresent(r -> {
            Assertions.assertEquals("1", r.getId(), "Review ID should be 1");
            Assertions.assertEquals(1, r.getProductId().intValue(), "Review Product ID should be 1");
            Assertions.assertEquals(1, r.getVersion().intValue(), "Review version should be 1");
            Assertions.assertEquals(1, r.getEntries().size(), "Review 1 should have one entry");
        });
    }

    @Test
    void testFindByIdFailure() {
        var review = repository.findById("99");
        Assertions.assertFalse(review.isPresent(), "We should not find a review with ID 99");
    }

    @Test
    void testFindByProductIdSuccess() {
        var review = repository.findByProductId(1);
        Assertions.assertTrue(review.isPresent(), "There should be a review for product ID 1");
    }

    @Test
    void testFindByProductIdFailure() {
        var review = repository.findByProductId(99);
        Assertions.assertFalse(review.isPresent(), "There should not be a review for product ID 99");
    }
}
