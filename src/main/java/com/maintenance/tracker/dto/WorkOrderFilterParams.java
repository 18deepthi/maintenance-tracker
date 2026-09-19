package com.maintenance.tracker.dto;

import com.maintenance.tracker.model.WorkOrderStatus;

public class WorkOrderFilterParams {

    private WorkOrderStatus status;
    private String assignedTo;
    private String equipmentName;
    private String equipmentId;

    public WorkOrderFilterParams() {
    }

    public WorkOrderFilterParams(WorkOrderStatus status, String assignedTo, String equipmentName, String equipmentId) {
        this.status = status;
        this.assignedTo = assignedTo;
        this.equipmentName = equipmentName;
        this.equipmentId = equipmentId;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
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
}