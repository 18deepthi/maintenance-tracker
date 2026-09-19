package com.maintenance.tracker.repository;

import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import org.springframework.data.jpa.domain.Specification;

public final class WorkOrderSpecifications {

    private WorkOrderSpecifications() {
    }

    public static Specification<WorkOrder> hasStatus(WorkOrderStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<WorkOrder> hasAssignedTo(String assignedTo) {
        return (root, query, cb) -> {
            if (assignedTo == null || assignedTo.isBlank()) {
                return null;
            }
            return cb.equal(cb.lower(root.get("assignedTo")), assignedTo.trim().toLowerCase());
        };
    }

    public static Specification<WorkOrder> hasEquipmentId(String equipmentId) {
        return (root, query, cb) -> {
            if (equipmentId == null || equipmentId.isBlank()) {
                return null;
            }
            return cb.equal(cb.lower(root.get("equipmentId")), equipmentId.trim().toLowerCase());
        };
    }

    public static Specification<WorkOrder> hasEquipmentName(String equipmentName) {
        return (root, query, cb) -> {
            if (equipmentName == null || equipmentName.isBlank()) {
                return null;
            }
            String escaped = equipmentName.trim()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            return cb.like(cb.lower(root.get("equipmentName")), "%" + escaped.toLowerCase() + "%", '\\');
        };
    }

    public static Specification<WorkOrder> withFilters(WorkOrderStatus status, String assignedTo,
                                                       String equipmentName, String equipmentId) {
        return Specification.where(hasStatus(status))
                .and(hasAssignedTo(assignedTo))
                .and(hasEquipmentName(equipmentName))
                .and(hasEquipmentId(equipmentId));
    }
}