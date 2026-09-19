package com.maintenance.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateWorkOrderRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Equipment name is required")
    @Size(max = 100, message = "Equipment name must not exceed 100 characters")
    private String equipmentName;

    @Size(max = 50, message = "Equipment ID must not exceed 50 characters")
    private String equipmentId;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @NotBlank(message = "Created by is required")
    @Size(max = 50, message = "Created by must not exceed 50 characters")
    private String createdBy;

    @Size(max = 50, message = "Assigned to must not exceed 50 characters")
    private String assignedTo;

    public CreateWorkOrderRequest() {
    }

    public CreateWorkOrderRequest(String title, String description, String equipmentName,
                                  String equipmentId, String location, String createdBy, String assignedTo) {
        this.title = title;
        this.description = description;
        this.equipmentName = equipmentName;
        this.equipmentId = equipmentId;
        this.location = location;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
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
}