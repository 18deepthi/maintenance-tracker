package com.maintenance.tracker.service;

import com.maintenance.tracker.dto.CreateWorkOrderRequest;
import com.maintenance.tracker.dto.PageResponse;
import com.maintenance.tracker.dto.UpdateWorkOrderDetailsRequest;
import com.maintenance.tracker.dto.WorkOrderResponse;
import com.maintenance.tracker.exception.InvalidWorkOrderStateException;
import com.maintenance.tracker.exception.ResourceNotFoundException;
import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import com.maintenance.tracker.repository.WorkOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkOrderService {

    public static final int MAX_PAGE_SIZE = 100;

    private final WorkOrderRepository workOrderRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
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
        int clampedSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable effectivePageable = PageRequest.of(pageable.getPageNumber(), clampedSize, pageable.getSort());

        Page<WorkOrder> page = workOrderRepository.findAll(effectivePageable);
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

        WorkOrder updated = workOrderRepository.save(workOrder);
        return WorkOrderResponse.fromEntity(updated);
    }
}