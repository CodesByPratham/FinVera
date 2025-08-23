package com.pratham.finvera.controller;

import com.pratham.finvera.payload.MessageResponse;
import com.pratham.finvera.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
