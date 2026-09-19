package com.maintenance.tracker.controller;

import com.maintenance.tracker.dto.CreateWorkOrderRequest;
import com.maintenance.tracker.dto.PageResponse;
import com.maintenance.tracker.dto.UpdateAssignmentRequest;
import com.maintenance.tracker.dto.UpdateStatusRequest;
import com.maintenance.tracker.dto.UpdateWorkOrderDetailsRequest;
import com.maintenance.tracker.dto.WorkOrderFilterParams;
import com.maintenance.tracker.dto.WorkOrderResponse;
import com.maintenance.tracker.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse createWorkOrder(@Valid @RequestBody CreateWorkOrderRequest request) {
        return workOrderService.createWorkOrder(request);
    }

    @GetMapping("/{id}")
    public WorkOrderResponse getWorkOrderById(@PathVariable Long id) {
        return workOrderService.getWorkOrderById(id);
    }

    @GetMapping
    public PageResponse<WorkOrderResponse> listWorkOrders(
            WorkOrderFilterParams filterParams,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return workOrderService.listWorkOrders(filterParams, pageable);
    }

    @PutMapping("/{id}")
    public WorkOrderResponse updateWorkOrderDetails(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkOrderDetailsRequest request) {
        return workOrderService.updateWorkOrderDetails(id, request);
    }

    @PatchMapping("/{id}/status")
    public WorkOrderResponse updateWorkOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return workOrderService.updateWorkOrderStatus(id, request);
    }

    @PatchMapping("/{id}/assignment")
    public WorkOrderResponse updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request) {
        return workOrderService.updateAssignment(id, request);
    }
}