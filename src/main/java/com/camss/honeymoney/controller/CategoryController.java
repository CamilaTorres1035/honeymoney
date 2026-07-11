package com.camss.honeymoney.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.camss.honeymoney.model.Category;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @GetMapping
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = Stream.of(Category.values()).map(Enum::name).toList();
        return ResponseEntity.ok(categories);
    }
}
