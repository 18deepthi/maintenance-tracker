package com.maintenance.tracker.dto;

import com.maintenance.tracker.model.WorkOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "Target work order status", example = "IN_PROGRESS")
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