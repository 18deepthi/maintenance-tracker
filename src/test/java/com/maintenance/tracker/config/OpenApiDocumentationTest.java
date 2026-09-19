package com.maintenance.tracker.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /v3/api-docs returns 200 and valid OpenAPI 3 definition")
    void getApiDocs_returnsOkAndOpenApiDefinition() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi", containsString("3.")))
                .andExpect(jsonPath("$.info.title").value("Maintenance Work-Order Tracker API"))
                .andExpect(jsonPath("$.paths['/api/v1/work-orders']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/work-orders/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/work-orders/{id}/status']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/work-orders/{id}/assignment']").exists());
    }
}