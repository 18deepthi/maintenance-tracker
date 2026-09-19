package com.maintenance.tracker.dto;

import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public class WorkOrderResponse {

    @Schema(description = "Unique work order identifier", example = "1")
    private Long id;

    @Schema(description = "Work order title", example = "HVAC Compressor Overheating")
    private String title;

    @Schema(description = "Work order description", example = "Basement chiller compressor trips on high temp")
    private String description;

    @Schema(description = "Equipment name", example = "Centrifugal Chiller 3")
    private String equipmentName;

    @Schema(description = "Equipment identifier", example = "CHILLER-B-03")
    private String equipmentId;

    @Schema(description = "Physical location", example = "Building B, Basement Mechanical Room")
    private String location;

    @Schema(description = "Username of creator", example = "john.doe")
    private String createdBy;

    @Schema(description = "Username of assignee", example = "jane.smith")
    private String assignedTo;

    @Schema(description = "Current work order status", example = "OPEN")
    private WorkOrderStatus status;

    @Schema(description = "Creation timestamp in UTC", example = "2026-09-19T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp in UTC", example = "2026-09-19T10:30:00Z")
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

    public static WorkOrderResponse fromEntity(WorkOrder workOrder) {
        if (workOrder == null) {
            return null;
        }
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getTitle(),
                workOrder.getDescription(),
                workOrder.getEquipmentName(),
                workOrder.getEquipmentId(),
                workOrder.getLocation(),
                workOrder.getCreatedBy(),
                workOrder.getAssignedTo(),
                workOrder.getStatus(),
                workOrder.getCreatedAt(),
                workOrder.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}