package com.example.qnr.constraints;

import com.example.qnr.dto.OrderDto;
import com.example.qnr.security.entities.AuthRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class ConstraintsTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void invalidOrderDto_ShouldRiseExceptionMessage_WhenDescriptionIsNullOrEmpty() {
        OrderDto order = new OrderDto(1, "", "PENDING", LocalDateTime.now());

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);
        assertFalse(violations.isEmpty());
        String firstMessage = violations.iterator().next().getMessage();
        assertEquals("The description can not be null or empty", firstMessage);
    }

    @Test
    void invalidOrderDto_ShouldRiseExceptionMessage_WhenStatusIsNullOrEmpty() {
        OrderDto order = new OrderDto(1, "Sample Order Description", "", LocalDateTime.now());

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);
        assertFalse(violations.isEmpty());
        String firstMessage = violations.iterator().next().getMessage();
        assertEquals("The status can not be null or empty", firstMessage);
    }

    @Test
    void usernameAndPassword_ShouldFailValidation_WhenBlank() {
        AuthRequest authRequest = new AuthRequest(" ", " ");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);

        assertThat(violations).hasSize(2);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "The username cannot be empty or just spaces",
                        "The password cannot be empty or just spaces"
                );
    }

    @Test
    void username_ShouldFailValidation_WhenBlank() {
        AuthRequest authRequest = new AuthRequest(" ", "ValidPassword123");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The username cannot be empty or just spaces");
    }

    @Test
    void password_ShouldFailValidation_WhenBlank() {
        AuthRequest authRequest = new AuthRequest("ValidUsername", " ");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The password cannot be empty or just spaces");
    }

    @Test
    void usernameAndPassword_ShouldPassValidation_WhenValid() {
        AuthRequest authRequest = new AuthRequest("ValidUser", "SecurePassword");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);
        assertThat(violations).isEmpty();
    }
}