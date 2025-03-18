package com.example.qnr.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException mockException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        FieldError fieldError1 = new FieldError("userDto", "username", "The username cannot be empty or just spaces");
        FieldError fieldError2 = new FieldError("userDto", "role", "The role cannot be null");

        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(fieldError1);
        fieldErrors.add(fieldError2);

        when(mockException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<Object> response = globalExceptionHandler
                .handleMethodArgumentNotValid(mockException, null, HttpStatus.BAD_REQUEST, webRequest);

        assert response.getStatusCode() == HttpStatus.BAD_REQUEST;
        assert response.getBody() instanceof java.util.Map;
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> errors = (java.util.Map<String, String>) response.getBody();
        assert errors.get("username").equals("The username cannot be empty or just spaces");
        assert errors.get("role").equals("The role cannot be null");
    }
}
