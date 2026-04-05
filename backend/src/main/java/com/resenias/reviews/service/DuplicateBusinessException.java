package com.resenias.reviews.service;

public class DuplicateBusinessException extends RuntimeException {

    public DuplicateBusinessException(String message) {
        super(message);
    }
}
