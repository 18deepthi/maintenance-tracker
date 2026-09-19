package com.maintenance.tracker.controller;

import com.maintenance.tracker.dto.CreateWorkOrderRequest;
import com.maintenance.tracker.dto.PageResponse;
import com.maintenance.tracker.dto.UpdateAssignmentRequest;
import com.maintenance.tracker.dto.UpdateStatusRequest;
import com.maintenance.tracker.dto.UpdateWorkOrderDetailsRequest;
import com.maintenance.tracker.dto.WorkOrderFilterParams;
import com.maintenance.tracker.dto.WorkOrderResponse;
import com.maintenance.tracker.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing maintenance work orders.
 * Requirements: FR-1, FR-2, FR-3, FR-4, FR-5, FR-6, FR-7.
 */
@RestController
@RequestMapping("/api/v1/work-orders")
@Tag(name = "Work Orders", description = "Operations for creating, managing, and tracking maintenance work orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    @Operation(summary = "Create a new work order", description = "Creates a work order in OPEN status.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Work order created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failure or malformed JSON")
    })
    public ResponseEntity<WorkOrderResponse> createWorkOrder(
            @Valid @RequestBody CreateWorkOrderRequest request) {
        WorkOrderResponse response = workOrderService.createWorkOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get work order by ID", description = "Retrieves a single work order by its identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Work order retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Work order not found")
    })
    public ResponseEntity<WorkOrderResponse> getWorkOrderById(
            @Parameter(description = "ID of the work order") @PathVariable Long id) {
        WorkOrderResponse response = workOrderService.getWorkOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List work orders",
            description = "Retrieves paginated, sorted, and filtered work orders.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Work orders retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid sort field, pagination param, or filter value")
    })
    public ResponseEntity<PageResponse<WorkOrderResponse>> getWorkOrders(
            @ModelAttribute WorkOrderFilterParams filterParams,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<WorkOrderResponse> response = workOrderService.listWorkOrders(filterParams, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update work order details",
            description = "Updates title, description, equipment, and location. Only allowed in OPEN status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Work order updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failure or malformed JSON"),
        @ApiResponse(responseCode = "404", description = "Work order not found"),
        @ApiResponse(responseCode = "409", description = "Work order is not in OPEN status or conflict")
    })
    public ResponseEntity<WorkOrderResponse> updateWorkOrderDetails(
            @Parameter(description = "ID of the work order") @PathVariable Long id,
            @Valid @RequestBody UpdateWorkOrderDetailsRequest request) {
        WorkOrderResponse response = workOrderService.updateWorkOrderDetails(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update work order status",
            description = "Transitions status strictly forward: OPEN -> IN_PROGRESS -> COMPLETED -> CLOSED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failure, missing status, or invalid enum value"),
        @ApiResponse(responseCode = "404", description = "Work order not found"),
        @ApiResponse(responseCode = "409", description = "Invalid status transition or conflict")
    })
    public ResponseEntity<WorkOrderResponse> updateWorkOrderStatus(
            @Parameter(description = "ID of the work order") @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        WorkOrderResponse response = workOrderService.updateWorkOrderStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/assignment")
    @Operation(summary = "Assign or reassign work order",
            description = "Assigns, reassigns, or unassigns a work order.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Assignment updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failure or malformed JSON"),
        @ApiResponse(responseCode = "404", description = "Work order not found"),
        @ApiResponse(responseCode = "409", description = "Invalid assignment operation or conflict")
    })
    public ResponseEntity<WorkOrderResponse> updateAssignment(
            @Parameter(description = "ID of the work order") @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request) {
        WorkOrderResponse response = workOrderService.updateAssignment(id, request);
        return ResponseEntity.ok(response);
    }
}