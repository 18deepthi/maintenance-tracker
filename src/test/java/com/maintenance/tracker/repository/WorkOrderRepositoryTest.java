package com.maintenance.tracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class WorkOrderRepositoryTest {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Saved work order defaults to OPEN status and populates both createdAt and updatedAt timestamps")
    void save_shouldDefaultToOpenStatusAndSetTimestamps() {
        WorkOrder workOrder = new WorkOrder(
                "HVAC Motor Vibration",
                "Excessive vibration observed on blower motor #3",
                "Blower Motor #3",
                "EQ-BM-003",
                "Building B, Basement HVAC Room",
                "supervisor_1",
                null
        );

        WorkOrder saved = workOrderRepository.save(workOrder);
        entityManager.flush();
        entityManager.clear();

        WorkOrder reloaded = entityManager.find(WorkOrder.class, saved.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getStatus()).isEqualTo(WorkOrderStatus.OPEN);
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getUpdatedAt()).isNotNull();
        assertThat(reloaded.getCreatedAt()).isEqualTo(reloaded.getUpdatedAt());
    }

    @Test
    @DisplayName("Updating a work order modifies updatedAt but keeps createdAt unchanged")
    void update_shouldNotChangeCreatedAt() throws InterruptedException {
        WorkOrder workOrder = new WorkOrder(
                "Transformer Oil Leak",
                "Minor seepage detected around gasket seal",
                "Step-down Transformer T-101",
                "TR-101",
                "Substation 1, Yard East",
                "supervisor_1",
                null
        );

        WorkOrder saved = workOrderRepository.save(workOrder);
        entityManager.flush();
        entityManager.clear();

        WorkOrder persisted = entityManager.find(WorkOrder.class, saved.getId());
        Instant initialCreatedAt = persisted.getCreatedAt();
        Instant initialUpdatedAt = persisted.getUpdatedAt();

        Thread.sleep(15);

        persisted.setDescription("Updated seepage notes after inspection");
        workOrderRepository.save(persisted);
        entityManager.flush();
        entityManager.clear();

        WorkOrder reloaded = entityManager.find(WorkOrder.class, saved.getId());
        assertThat(reloaded.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(reloaded.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt);
    }
}