package com.maintenance.tracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateWorkOrderDetailsRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    @Schema(description = "Updated work order title", example = "HVAC Compressor Overheating - Urgent")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Updated work order description", example = "Compressor trips within 10 mins of startup")
    private String description;

    @NotBlank(message = "Equipment name is required")
    @Size(max = 100, message = "Equipment name must not exceed 100 characters")
    @Schema(description = "Updated equipment name", example = "Centrifugal Chiller 3")
    private String equipmentName;

    @Size(max = 50, message = "Equipment ID must not exceed 50 characters")
    @Schema(description = "Updated equipment identifier", example = "CHILLER-B-03")
    private String equipmentId;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location must not exceed 150 characters")
    @Schema(description = "Updated physical location", example = "Building B, Basement Mechanical Room")
    private String location;

    public UpdateWorkOrderDetailsRequest() {
    }

    public UpdateWorkOrderDetailsRequest(String title, String description, String equipmentName,
                                         String equipmentId, String location) {
        this.title = title;
        this.description = description;
        this.equipmentName = equipmentName;
        this.equipmentId = equipmentId;
        this.location = location;
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
}