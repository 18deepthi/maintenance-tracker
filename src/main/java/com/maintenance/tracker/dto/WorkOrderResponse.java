package com.maintenance.tracker.dto;

import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import java.time.Instant;

public class WorkOrderResponse {

    private Long id;
    private String title;
    private String description;
    private String equipmentName;
    private String equipmentId;
    private String location;
    private String createdBy;
    private String assignedTo;
    private WorkOrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public WorkOrderResponse() {
    }

    public WorkOrderResponse(Long id, String title, String description, String equipmentName,
                             String equipmentId, String location, String createdBy, String assignedTo,
                             WorkOrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.equipmentName = equipmentName;
        this.equipmentId = equipmentId;
        this.location = location;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WorkOrderResponse fromEntity(WorkOrder order) {
        if (order == null) {
            return null;
        }
        return new WorkOrderResponse(
                order.getId(),
                order.getTitle(),
                order.getDescription(),
                order.getEquipmentName(),
                order.getEquipmentId(),
                order.getLocation(),
                order.getCreatedBy(),
                order.getAssignedTo(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getLocation() {
        return location;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}