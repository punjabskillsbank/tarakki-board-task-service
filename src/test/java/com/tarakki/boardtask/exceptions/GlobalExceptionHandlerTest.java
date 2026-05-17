package com.tarakki.boardtask.exceptions;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.exception.GlobalExceptionHandler;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.common.exceptionHandling.OrganisationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldHandleValidationExceptions() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.boardName").value("must not be blank"))
                .andExpect(jsonPath("$.orgId").value("must not be null"));
    }

    @Test
    void shouldHandleOrganisationNotFoundException() throws Exception {
        Long missingOrgId = BoardTestDataFactory.INVALID_ORG_ID;

        mockMvc.perform(get("/test/org-not-found/{id}", missingOrgId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Organisation with id " + missingOrgId + " not found"));
    }

    @RestController
    static class TestController {
        @PostMapping("/test/validate")
        public void validate(@org.springframework.validation.annotation.Validated @RequestBody BoardDTO dto) {
        }

        @GetMapping("/test/org-not-found/{id}")
        public void throwOrgNotFound() {
            throw new OrganisationNotFoundException(BoardTestDataFactory.INVALID_ORG_ID);
        }
    }
}
