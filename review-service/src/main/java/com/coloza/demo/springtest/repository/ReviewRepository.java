package com.coloza.demo.springtest.repository;

import com.coloza.demo.springtest.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

    Optional<Review> findByProductId(Integer productId);
}
