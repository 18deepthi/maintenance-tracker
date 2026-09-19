package com.maintenance.tracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class UpdateAssignmentRequest {

    @Size(max = 50, message = "Assigned to must not exceed 50 characters")
    @Schema(description = "Username to assign, or blank/null to unassign", example = "jane.smith")
    private String assignedTo;

    public UpdateAssignmentRequest() {
    }

    public UpdateAssignmentRequest(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}