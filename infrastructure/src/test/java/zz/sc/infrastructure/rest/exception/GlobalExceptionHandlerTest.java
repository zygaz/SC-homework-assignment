package zz.sc.infrastructure.rest.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import zz.sc.exception.ContentModificationNotAllowedException;
import zz.sc.exception.DomainException;
import zz.sc.exception.InvalidStateTransitionException;
import zz.sc.exception.RequestNotFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldHandleRequestNotFoundExceptionAs404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.detail").value("Request not found"));
    }

    @Test
    void shouldHandleInvalidStateTransitionExceptionAs409() throws Exception {
        mockMvc.perform(get("/test/invalid-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.detail").value("Invalid state transition"));
    }

    @Test
    void shouldHandleContentModificationNotAllowedExceptionAs409() throws Exception {
        mockMvc.perform(get("/test/content-modification"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.detail").value("Content modification not allowed"));
    }

    @Test
    void shouldHandleGenericDomainExceptionAs400() throws Exception {
        mockMvc.perform(get("/test/generic-domain"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.detail").value("Generic domain error"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new RequestNotFoundException("Request not found");
        }

        @GetMapping("/test/invalid-state")
        public void throwInvalidState() {
            throw new InvalidStateTransitionException("Invalid state transition");
        }

        @GetMapping("/test/content-modification")
        public void throwContentModification() {
            throw new ContentModificationNotAllowedException("Content modification not allowed");
        }

        @GetMapping("/test/generic-domain")
        public void throwGenericDomain() {
            throw new DomainException("Generic domain error") {};
        }
    }
}
