package com.maintenance.tracker.exception;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.maintenance.tracker.controller.WorkOrderController;
import com.maintenance.tracker.dto.UpdateStatusRequest;
import com.maintenance.tracker.dto.UpdateWorkOrderDetailsRequest;
import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.service.WorkOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
@WebMvcTest(controllers = WorkOrderController.class)
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private WorkOrderService workOrderService;
    @Test
    @DisplayName("400 Bad Request: validation error populates standard error schema with fieldErrors")
    void handleValidationException_shouldReturn400WithFieldErrors() throws Exception {
        String invalidJson = """
                {
                    "title": "",
                    "equipmentName": "",
                    "location": "",
                    "createdBy": ""
                }
                """;
        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/work-orders"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(4)));
    }
    @Test
    @DisplayName("400 Bad Request: malformed JSON returns plain message without class names")
    void handleMessageNotReadable_malformedJson_shouldReturn400PlainMessage() throws Exception {
        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed_json_here"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid field format."))
                .andExpect(jsonPath("$.message", not(containsString("com.fasterxml.jackson"))))
                .andExpect(jsonPath("$.message", not(containsString("java.lang"))))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("400 Bad Request: invalid enum in body returns plain message without class names")
    void handleMessageNotReadable_invalidEnumInBody_shouldReturn400PlainMessage() throws Exception {
        mockMvc.perform(patch("/api/v1/work-orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"BANANA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid field format."))
                .andExpect(jsonPath("$.message", not(containsString("com.maintenance.tracker"))))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("400 Bad Request: invalid enum in query parameter returns 400")
    void handleTypeMismatch_invalidEnumInQuery_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/work-orders?status=BANANA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"))
                .andExpect(jsonPath("$.fieldErrors[0].message", containsString("BANANA")));
    }
    @Test
    @DisplayName("400 Bad Request: invalid sort field returns 400")
    void handleSortException_invalidSortField_shouldReturn400() throws Exception {
        when(workOrderService.listWorkOrders(any(), any()))
                .thenThrow(new InvalidSortFieldException("Invalid sort field: 'banana'. Allowed sort fields are: [id]"));
        mockMvc.perform(get("/api/v1/work-orders?sort=banana"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("banana")))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("404 Not Found: ResourceNotFoundException returns 404")
    void handleResourceNotFound_shouldReturn404() throws Exception {
        when(workOrderService.getWorkOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Work order not found with ID: 999"));
        mockMvc.perform(get("/api/v1/work-orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Work order not found with ID: 999"))
                .andExpect(jsonPath("$.path").value("/api/v1/work-orders/999"))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("404 Not Found: unknown URL returns 404 in standard ErrorResponse schema")
    void handleUnknownUrl_shouldReturn404StandardSchema() throws Exception {
        mockMvc.perform(get("/api/v1/unknown-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/unknown-endpoint"))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("405 Method Not Allowed: unsupported HTTP method returns 405 in standard schema")
    void handleMethodNotSupported_shouldReturn405StandardSchema() throws Exception {
        mockMvc.perform(post("/api/v1/work-orders/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message", containsString("POST")))
                .andExpect(jsonPath("$.path").value("/api/v1/work-orders/1"))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("415 Unsupported Media Type: unsupported content type returns 415 in standard schema")
    void handleMediaTypeNotSupported_shouldReturn415StandardSchema() throws Exception {
        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<workOrder><title>Test</title></workOrder>"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.message", containsString("Content-Type")))
                .andExpect(jsonPath("$.path").value("/api/v1/work-orders"))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("409 Conflict: InvalidWorkOrderStateException returns 409 in standard schema")
    void handleInvalidWorkOrderState_shouldReturn409() throws Exception {
        when(workOrderService.updateWorkOrderStatus(eq(1L), any(UpdateStatusRequest.class)))
                .thenThrow(new InvalidWorkOrderStateException("Invalid status transition from OPEN to CLOSED."));
        mockMvc.perform(patch("/api/v1/work-orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Invalid status transition from OPEN to CLOSED."))
                .andExpect(jsonPath("$.path").value("/api/v1/work-orders/1/status"))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("409 Conflict: optimistic locking failure returns 409 in standard schema")
    void handleOptimisticLocking_shouldReturn409() throws Exception {
        when(workOrderService.updateWorkOrderDetails(eq(1L), any(UpdateWorkOrderDetailsRequest.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(WorkOrder.class, 1L));
        String updateJson = """
                {
                    "title": "New Title",
                    "equipmentName": "New Eq",
                    "location": "New Loc"
                }
                """;
        mockMvc.perform(put("/api/v1/work-orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("updated by another transaction")))
                .andExpect(jsonPath("$.path").value("/api/v1/work-orders/1"))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
    @Test
    @DisplayName("500 Internal Server Error: unexpected exception returns generic message without stack trace")
    void handleGenericException_shouldReturn500WithoutStackTrace() throws Exception {
        when(workOrderService.getWorkOrderById(500L))
                .thenThrow(new RuntimeException("Critical database connection dropped!"));
        mockMvc.perform(get("/api/v1/work-orders/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact support."))
                .andExpect(jsonPath("$.message", not(containsString("Critical database"))))
                .andExpect(jsonPath("$.path").value("/api/v1/work-orders/500"))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }
}
