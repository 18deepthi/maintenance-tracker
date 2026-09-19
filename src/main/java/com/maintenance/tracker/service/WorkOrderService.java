package com.maintenance.tracker.service;

import com.maintenance.tracker.dto.CreateWorkOrderRequest;
import com.maintenance.tracker.dto.PageResponse;
import com.maintenance.tracker.dto.UpdateAssignmentRequest;
import com.maintenance.tracker.dto.UpdateStatusRequest;
import com.maintenance.tracker.dto.UpdateWorkOrderDetailsRequest;
import com.maintenance.tracker.dto.WorkOrderFilterParams;
import com.maintenance.tracker.dto.WorkOrderResponse;
import com.maintenance.tracker.exception.InvalidWorkOrderStateException;
import com.maintenance.tracker.exception.ResourceNotFoundException;
import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import com.maintenance.tracker.repository.WorkOrderRepository;
import com.maintenance.tracker.repository.WorkOrderSpecifications;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkOrderService {

    public static final int MAX_PAGE_SIZE = 100;

    // FR-3: Strictly one-way sequential lifecycle OPEN -> IN_PROGRESS -> COMPLETED -> CLOSED
    private static final Map<WorkOrderStatus, WorkOrderStatus> ALLOWED_TRANSITIONS = Map.of(
            WorkOrderStatus.OPEN, WorkOrderStatus.IN_PROGRESS,
            WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.COMPLETED,
            WorkOrderStatus.COMPLETED, WorkOrderStatus.CLOSED
    );

    private final WorkOrderRepository workOrderRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public static boolean isValidTransition(WorkOrderStatus current, WorkOrderStatus target) {
        return ALLOWED_TRANSITIONS.get(current) == target;
    }

    @Transactional
    public WorkOrderResponse createWorkOrder(CreateWorkOrderRequest request) {
        WorkOrder workOrder = new WorkOrder(
                request.getTitle(),
                request.getDescription(),
                request.getEquipmentName(),
                request.getEquipmentId(),
                request.getLocation(),
                request.getCreatedBy(),
                request.getAssignedTo()
        );

        WorkOrder saved = workOrderRepository.save(workOrder);
        return WorkOrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getWorkOrderById(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));
        return WorkOrderResponse.fromEntity(workOrder);
    }

    @Transactional(readOnly = true)
    public PageResponse<WorkOrderResponse> listWorkOrders(Pageable pageable) {
        return listWorkOrders(null, pageable);
    }

    // FR-7, FR-9: Composable filtering alongside pagination and sorting
    @Transactional(readOnly = true)
    public PageResponse<WorkOrderResponse> listWorkOrders(WorkOrderFilterParams filterParams, Pageable pageable) {
        int clampedSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable effectivePageable = PageRequest.of(pageable.getPageNumber(), clampedSize, pageable.getSort());

        Specification<WorkOrder> spec = filterParams == null
                ? null
                : WorkOrderSpecifications.withFilters(
                        filterParams.getStatus(),
                        filterParams.getAssignedTo(),
                        filterParams.getEquipmentName(),
                        filterParams.getEquipmentId()
                );

        Page<WorkOrder> page = spec == null
                ? workOrderRepository.findAll(effectivePageable)
                : workOrderRepository.findAll(spec, effectivePageable);

        return PageResponse.fromPage(page.map(WorkOrderResponse::fromEntity));
    }

    @Transactional
    public WorkOrderResponse updateWorkOrderDetails(Long id, UpdateWorkOrderDetailsRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        // FR-8: The system shall allow updating core work order details only while in OPEN status.
        if (workOrder.getStatus() != WorkOrderStatus.OPEN) {
            throw new InvalidWorkOrderStateException(
                    "Cannot update details for work order with ID " + id
                            + " because it is in " + workOrder.getStatus()
                            + " status. Updates are permitted only while status is OPEN (FR-8)."
            );
        }

        workOrder.setTitle(request.getTitle());
        workOrder.setDescription(request.getDescription());
        workOrder.setEquipmentName(request.getEquipmentName());
        workOrder.setEquipmentId(request.getEquipmentId());
        workOrder.setLocation(request.getLocation());

        WorkOrder updated = workOrderRepository.saveAndFlush(workOrder);
        return WorkOrderResponse.fromEntity(updated);
    }

    // FR-3, FR-4, FR-5: State transition enforcement
    @Transactional
    public WorkOrderResponse updateWorkOrderStatus(Long id, UpdateStatusRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        WorkOrderStatus currentStatus = workOrder.getStatus();
        WorkOrderStatus targetStatus = request.getStatus();

        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new InvalidWorkOrderStateException(
                    "Invalid status transition from " + currentStatus + " to " + targetStatus
                            + ". Work orders can only transition forward strictly one step: "
                            + "OPEN -> IN_PROGRESS -> COMPLETED -> CLOSED (FR-3, FR-4)."
            );
        }

        if (targetStatus == WorkOrderStatus.IN_PROGRESS
                && (workOrder.getAssignedTo() == null || workOrder.getAssignedTo().isBlank())) {
            throw new InvalidWorkOrderStateException(
                    "Cannot transition work order with ID " + id
                            + " to IN_PROGRESS without an assigned engineer (FR-5)."
            );
        }

        workOrder.setStatus(targetStatus);
        WorkOrder updated = workOrderRepository.saveAndFlush(workOrder);
        return WorkOrderResponse.fromEntity(updated);
    }

    // FR-5: Assignment lifecycle management
    @Transactional
    public WorkOrderResponse updateAssignment(Long id, UpdateAssignmentRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        String rawAssignee = request.getAssignedTo();
        String trimmedAssignee = (rawAssignee == null || rawAssignee.isBlank()) ? null : rawAssignee.trim();

        WorkOrderStatus currentStatus = workOrder.getStatus();

        if (currentStatus == WorkOrderStatus.COMPLETED || currentStatus == WorkOrderStatus.CLOSED) {
            throw new InvalidWorkOrderStateException(
                    "Cannot modify assignment for work order with ID " + id
                            + " in " + currentStatus + " status. Assignment modifications are not permitted once "
                            + "COMPLETED or CLOSED (FR-5)."
            );
        }

        if (trimmedAssignee == null && currentStatus != WorkOrderStatus.OPEN) {
            throw new InvalidWorkOrderStateException(
                    "Cannot unassign work order with ID " + id
                            + " while in " + currentStatus
                            + " status. Work orders can only be unassigned while in OPEN status (FR-5)."
            );
        }

        workOrder.setAssignedTo(trimmedAssignee);
        WorkOrder updated = workOrderRepository.saveAndFlush(workOrder);
        return WorkOrderResponse.fromEntity(updated);
    }
}
