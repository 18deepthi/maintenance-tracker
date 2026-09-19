package com.maintenance.tracker.dto;

import com.maintenance.tracker.model.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Target status is required")
    private WorkOrderStatus status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(WorkOrderStatus status) {
        this.status = status;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }
}