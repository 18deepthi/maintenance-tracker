package com.maintenance.tracker.dto;

import jakarta.validation.constraints.Size;

public class UpdateAssignmentRequest {

    @Size(max = 50, message = "Assigned to must not exceed 50 characters")
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