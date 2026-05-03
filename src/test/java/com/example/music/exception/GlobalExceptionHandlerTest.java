package com.example.music.exception;

import com.example.music.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("Business error");

        ResponseEntity<ApiResponse<Void>> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Business error", response.getBody().getMessage());
    }

    @Test
    void handleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentialsException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid username or password", response.getBody().getMessage());
    }

    @Test
    void handleUsernameNotFoundException() {
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUsernameNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void handleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024);

        ResponseEntity<ApiResponse<Void>> response = handler.handleMaxUploadSizeExceededException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("File size exceeds the maximum limit", response.getBody().getMessage());
    }

    @Test
    void handleInvalidFileTypeException() {
        InvalidFileTypeException ex = new InvalidFileTypeException("Invalid file type");

        ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidFileTypeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid file type", response.getBody().getMessage());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Unexpected");

        ResponseEntity<ApiResponse<Void>> response = handler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }
}
