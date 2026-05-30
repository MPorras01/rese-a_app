package com.resenias.reviewsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.resenias")
@EntityScan(basePackages = "com.resenias.reviews.entity")
@EnableJpaRepositories(basePackages = "com.resenias.reviews.repository")
public class ReviewsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewsApiApplication.class, args);
    }
}
