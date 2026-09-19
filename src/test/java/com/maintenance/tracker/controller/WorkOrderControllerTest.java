package com.maintenance.tracker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maintenance.tracker.dto.CreateWorkOrderRequest;
import com.maintenance.tracker.dto.PageResponse;
import com.maintenance.tracker.dto.UpdateWorkOrderDetailsRequest;
import com.maintenance.tracker.dto.WorkOrderResponse;
import com.maintenance.tracker.exception.InvalidWorkOrderStateException;
import com.maintenance.tracker.exception.ResourceNotFoundException;
import com.maintenance.tracker.model.WorkOrderStatus;
import com.maintenance.tracker.service.WorkOrderService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkOrderController.class)
class WorkOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkOrderService workOrderService;

    @Test
    @DisplayName("POST /api/v1/work-orders returns 201 Created and response body when input is valid")
    void createWorkOrder_whenValidRequest_shouldReturn201Created() throws Exception {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest(
                "Bearing Replacement",
                "Replace main bearing assembly",
                "Conveyor Motor #2",
                "EQ-MOT-002",
                "Factory Floor 1, Line A",
                "supervisor_a",
                "engineer_b"
        );

        WorkOrderResponse response = new WorkOrderResponse(
                1L,
                request.getTitle(),
                request.getDescription(),
                request.getEquipmentName(),
                request.getEquipmentId(),
                request.getLocation(),
                request.getCreatedBy(),
                request.getAssignedTo(),
                WorkOrderStatus.OPEN,
                Instant.now(),
                Instant.now()
        );

        when(workOrderService.createWorkOrder(any(CreateWorkOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Bearing Replacement"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("POST /api/v1/work-orders returns 400 Bad Request when title is blank")
    void createWorkOrder_whenBlankTitle_shouldReturn400BadRequest() throws Exception {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest(
                "   ",
                "Replace main bearing assembly",
                "Conveyor Motor #2",
                "EQ-MOT-002",
                "Factory Floor 1, Line A",
                "supervisor_a",
                null
        );

        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/work-orders returns 400 Bad Request when location is blank")
    void createWorkOrder_whenBlankLocation_shouldReturn400BadRequest() throws Exception {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest(
                "Bearing Replacement",
                "Replace main bearing assembly",
                "Conveyor Motor #2",
                "EQ-MOT-002",
                "",
                "supervisor_a",
                null
        );

        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/work-orders/{id} returns 200 OK when found")
    void getWorkOrderById_whenFound_shouldReturn200Ok() throws Exception {
        WorkOrderResponse response = new WorkOrderResponse(
                5L, "Calibrate Sensor", "Calibration check", "Temp Sensor TS-1",
                "EQ-TS-01", "Zone 3", "supervisor_a", null, WorkOrderStatus.OPEN,
                Instant.now(), Instant.now()
        );

        when(workOrderService.getWorkOrderById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/work-orders/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.title").value("Calibrate Sensor"));
    }

    @Test
    @DisplayName("GET /api/v1/work-orders/{id} returns 404 Not Found when order does not exist")
    void getWorkOrderById_whenNotFound_shouldReturn404NotFound() throws Exception {
        when(workOrderService.getWorkOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Work order not found with ID: 999"));

        mockMvc.perform(get("/api/v1/work-orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/work-orders returns 200 OK with PageResponse")
    void listWorkOrders_shouldReturn200OkWithPagedContent() throws Exception {
        WorkOrderResponse item = new WorkOrderResponse(
                1L, "Title", "Desc", "Eq", "EQ-1", "Loc", "admin", null,
                WorkOrderStatus.OPEN, Instant.now(), Instant.now()
        );
        PageResponse<WorkOrderResponse> pageResponse = new PageResponse<>(
                List.of(item), 0, 20, 1L, 1
        );

        when(workOrderService.listWorkOrders(any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/work-orders?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/work-orders/{id} returns 200 OK when valid and order is OPEN")
    void updateWorkOrderDetails_whenValidAndOpen_shouldReturn200Ok() throws Exception {
        UpdateWorkOrderDetailsRequest request = new UpdateWorkOrderDetailsRequest(
                "Updated Title", "Updated Description", "Updated Equip", "EQ-UPD", "Substation 2"
        );

        WorkOrderResponse updatedResponse = new WorkOrderResponse(
                1L,
                request.getTitle(),
                request.getDescription(),
                request.getEquipmentName(),
                request.getEquipmentId(),
                request.getLocation(),
                "supervisor_a",
                null,
                WorkOrderStatus.OPEN,
                Instant.now(),
                Instant.now()
        );

        when(workOrderService.updateWorkOrderDetails(eq(1L), any(UpdateWorkOrderDetailsRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/work-orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.location").value("Substation 2"));
    }

    @Test
    @DisplayName("PUT /api/v1/work-orders/{id} returns 400 Bad Request when title is blank")
    void updateWorkOrderDetails_whenBlankTitle_shouldReturn400BadRequest() throws Exception {
        UpdateWorkOrderDetailsRequest request = new UpdateWorkOrderDetailsRequest(
                "", "Updated Description", "Updated Equip", "EQ-UPD", "Substation 2"
        );

        mockMvc.perform(put("/api/v1/work-orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/work-orders/{id} returns 400 Bad Request when location is blank")
    void updateWorkOrderDetails_whenBlankLocation_shouldReturn400BadRequest() throws Exception {
        UpdateWorkOrderDetailsRequest request = new UpdateWorkOrderDetailsRequest(
                "Valid Title", "Updated Description", "Updated Equip", "EQ-UPD", "   "
        );

        mockMvc.perform(put("/api/v1/work-orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/work-orders/{id} returns 409 Conflict when status is not OPEN (FR-8)")
    void updateWorkOrderDetails_whenStatusIsNotOpen_shouldReturn409Conflict() throws Exception {
        UpdateWorkOrderDetailsRequest request = new UpdateWorkOrderDetailsRequest(
                "Updated Title", "Updated Description", "Updated Equip", "EQ-UPD", "Substation 2"
        );

        when(workOrderService.updateWorkOrderDetails(eq(2L), any(UpdateWorkOrderDetailsRequest.class)))
                .thenThrow(new InvalidWorkOrderStateException("Updates permitted only while status is OPEN (FR-8)."));

        mockMvc.perform(put("/api/v1/work-orders/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}