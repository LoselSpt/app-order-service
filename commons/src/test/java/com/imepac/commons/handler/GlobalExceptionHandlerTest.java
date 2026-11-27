package com.imepac.commons.handler;

import com.imepac.commons.exception.BusinessException;
import com.imepac.commons.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("returns 404 for EntityNotFoundException")
    void notFound() throws Exception {
        mockMvc.perform(get("/dummy/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Thing not found"));
    }

    @Test
    @DisplayName("returns 422 for BusinessException")
    void businessRule() throws Exception {
        mockMvc.perform(get("/dummy/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Rule violated"));
    }

    @Test
    @DisplayName("returns 400 with field names on validation errors")
    void validation() throws Exception {
        mockMvc.perform(post("/dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors", hasItem(containsString("name: "))));
    }

    @Test
    @DisplayName("returns 400 instead of 500 for malformed JSON")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    @DisplayName("returns 400 instead of 500 for path variable of wrong type")
    void typeMismatch() throws Exception {
        mockMvc.perform(get("/dummy/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("parameter 'id'")));
    }

    @Test
    @DisplayName("returns 405 for unsupported HTTP method")
    void methodNotAllowed() throws Exception {
        mockMvc.perform(delete("/dummy"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("returns 500 with generic message for unexpected errors")
    void unexpected() throws Exception {
        mockMvc.perform(get("/dummy/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    record DummyRequest(@NotBlank String name) {}

    @RestController
    @RequestMapping("/dummy")
    static class DummyController {

        @GetMapping("/{id}")
        String byId(@PathVariable Long id) {
            return "ok";
        }

        @GetMapping("/not-found")
        String notFound() {
            throw new EntityNotFoundException("Thing not found");
        }

        @GetMapping("/business")
        String business() {
            throw new BusinessException("Rule violated");
        }

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("boom");
        }

        @PostMapping
        String create(@Valid @RequestBody DummyRequest request) {
            return request.name();
        }
    }
}
