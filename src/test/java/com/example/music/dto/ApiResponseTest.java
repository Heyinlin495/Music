package com.example.music.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_WithData() {
        ApiResponse<String> response = ApiResponse.success("test data");

        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals("test data", response.getData());
    }

    @Test
    void success_WithMessageAndData() {
        ApiResponse<Integer> response = ApiResponse.success("Operation completed", 42);

        assertTrue(response.isSuccess());
        assertEquals("Operation completed", response.getMessage());
        assertEquals(42, response.getData());
    }

    @Test
    void error_WithMessage() {
        ApiResponse<Void> response = ApiResponse.error("Something went wrong");

        assertFalse(response.isSuccess());
        assertEquals("Something went wrong", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void success_NullData() {
        ApiResponse<Object> response = ApiResponse.success(null);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    void builder_CreatesCorrectly() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true).message("msg").data("val").build();

        assertTrue(response.isSuccess());
        assertEquals("msg", response.getMessage());
        assertEquals("val", response.getData());
    }
}
