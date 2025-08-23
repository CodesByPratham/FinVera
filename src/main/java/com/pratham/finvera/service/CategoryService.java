package com.pratham.finvera.service;

import com.pratham.finvera.payload.GetCategoryResponse;
import com.pratham.finvera.payload.MessageResponse;
import com.pratham.finvera.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public MessageResponse getAllCategories() {
        return GetCategoryResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK)
                .message("Categories and Sub Categories")
                .category(categoryRepository.findAll())
                .build();
    }
}
