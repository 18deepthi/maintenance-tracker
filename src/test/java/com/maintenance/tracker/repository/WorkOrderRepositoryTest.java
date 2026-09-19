package com.maintenance.tracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

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

    @Test
    @DisplayName("Status or assignment change updates updatedAt timestamp")
    void updateStatusAndAssignment_shouldUpdateUpdatedAtTimestamp() throws InterruptedException {
        WorkOrder workOrder = new WorkOrder(
                "Pump Seal Replacement", "Desc", "Centrifugal Pump P-1",
                "EQ-PUMP-01", "Zone A", "supervisor_1", null
        );

        WorkOrder saved = workOrderRepository.save(workOrder);
        entityManager.flush();
        entityManager.clear();

        WorkOrder persisted = entityManager.find(WorkOrder.class, saved.getId());
        Instant originalUpdatedAt = persisted.getUpdatedAt();

        Thread.sleep(15);

        persisted.setAssignedTo("engineer_x");
        persisted.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrderRepository.save(persisted);
        entityManager.flush();
        entityManager.clear();

        WorkOrder reloaded = entityManager.find(WorkOrder.class, saved.getId());
        assertThat(reloaded.getAssignedTo()).isEqualTo("engineer_x");
        assertThat(reloaded.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
        assertThat(reloaded.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("Filtering by status finds matching records")
    void filterByStatus_shouldReturnMatchingOrders() {
        WorkOrder order1 = new WorkOrder("WO 1", "Desc", "Pump A", "EQ-1", "Loc 1", "sup1", "eng1");
        WorkOrder order2 = new WorkOrder("WO 2", "Desc", "Pump B", "EQ-2", "Loc 2", "sup1", "eng1");
        order2.setStatus(WorkOrderStatus.IN_PROGRESS);

        workOrderRepository.saveAll(List.of(order1, order2));
        entityManager.flush();
        entityManager.clear();

        Specification<WorkOrder> spec = WorkOrderSpecifications
                .withFilters(WorkOrderStatus.IN_PROGRESS, null, null, null);
        Page<WorkOrder> result = workOrderRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("WO 2");
    }

    @Test
    @DisplayName("Filtering by assignedTo performs exact case-insensitive match")
    void filterByAssignedTo_shouldPerformExactCaseInsensitiveMatch() {
        WorkOrder order1 = new WorkOrder("WO 1", "Desc", "Eq 1", "E-1", "Loc 1", "sup1", "engineer_john");
        WorkOrder order2 = new WorkOrder("WO 2", "Desc", "Eq 2", "E-2", "Loc 2", "sup1", "engineer_johnson");
        workOrderRepository.saveAll(List.of(order1, order2));
        entityManager.flush();
        entityManager.clear();

        Specification<WorkOrder> spec = WorkOrderSpecifications.withFilters(null, "ENGINEER_JOHN", null, null);
        Page<WorkOrder> result = workOrderRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("WO 1");
    }

    @Test
    @DisplayName("Filtering by equipmentId performs exact case-insensitive match")
    void filterByEquipmentId_shouldPerformExactCaseInsensitiveMatch() {
        WorkOrder order1 = new WorkOrder("WO 1", "Desc", "Eq 1", "EQ-PUMP-01", "Loc 1", "sup1", null);
        WorkOrder order2 = new WorkOrder("WO 2", "Desc", "Eq 2", "EQ-PUMP-01-SUB", "Loc 2", "sup1", null);
        workOrderRepository.saveAll(List.of(order1, order2));
        entityManager.flush();
        entityManager.clear();

        Specification<WorkOrder> spec = WorkOrderSpecifications.withFilters(null, null, null, "eq-pump-01");
        Page<WorkOrder> result = workOrderRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("WO 1");
    }

    @Test
    @DisplayName("Filtering by equipmentName performs case-insensitive contains search and escapes wildcards")
    void filterByEquipmentName_shouldPerformContainsAndEscapeWildcards() {
        WorkOrder order1 = new WorkOrder(
                "WO 1", "Desc", "High Voltage 100% Transformer", "EQ-1", "Loc 1", "sup1", null);
        WorkOrder order2 = new WorkOrder("WO 2", "Desc", "Standard 100A Transformer", "EQ-2", "Loc 2", "sup1", null);
        WorkOrder order3 = new WorkOrder("WO 3", "Desc", "Feed_Water_Pump", "EQ-3", "Loc 3", "sup1", null);
        WorkOrder order4 = new WorkOrder("WO 4", "Desc", "FeedXWaterYPump", "EQ-4", "Loc 4", "sup1", null);
        workOrderRepository.saveAll(List.of(order1, order2, order3, order4));
        entityManager.flush();
        entityManager.clear();

        // 1. Case-insensitive substring match
        Specification<WorkOrder> subSpec = WorkOrderSpecifications.withFilters(null, null, "voltage", null);
        Page<WorkOrder> subResult = workOrderRepository.findAll(subSpec, PageRequest.of(0, 10));
        assertThat(subResult.getContent()).hasSize(1);
        assertThat(subResult.getContent().get(0).getTitle()).isEqualTo("WO 1");

        // 2. Literal '%' escape match
        Specification<WorkOrder> percentSpec = WorkOrderSpecifications.withFilters(null, null, "100%", null);
        Page<WorkOrder> percentResult = workOrderRepository.findAll(percentSpec, PageRequest.of(0, 10));
        assertThat(percentResult.getContent()).hasSize(1);
        assertThat(percentResult.getContent().get(0).getTitle()).isEqualTo("WO 1");

        // 3. Literal '_' escape match
        Specification<WorkOrder> underscoreSpec = WorkOrderSpecifications.withFilters(null, null, "water_pump", null);
        Page<WorkOrder> underscoreResult = workOrderRepository.findAll(underscoreSpec, PageRequest.of(0, 10));
        assertThat(underscoreResult.getContent()).hasSize(1);
        assertThat(underscoreResult.getContent().get(0).getTitle()).isEqualTo("WO 3");
    }

    @Test
    @DisplayName("Combining multiple filter criteria returns only matching records")
    void filterByMultipleCriteria_shouldCombinePredicates() {
        WorkOrder order1 = new WorkOrder("WO 1", "Desc", "Transformer A", "TR-A", "Loc 1", "sup1", "eng1");
        WorkOrder order2 = new WorkOrder("WO 2", "Desc", "Transformer A", "TR-A", "Loc 2", "sup1", "eng2");
        WorkOrder order3 = new WorkOrder("WO 3", "Desc", "Transformer B", "TR-B", "Loc 3", "sup1", "eng1");
        order1.setStatus(WorkOrderStatus.OPEN);
        order2.setStatus(WorkOrderStatus.OPEN);
        order3.setStatus(WorkOrderStatus.IN_PROGRESS);

        workOrderRepository.saveAll(List.of(order1, order2, order3));
        entityManager.flush();
        entityManager.clear();

        Specification<WorkOrder> spec = WorkOrderSpecifications
                .withFilters(WorkOrderStatus.OPEN, "eng1", "Transformer A", "TR-A");
        Page<WorkOrder> result = workOrderRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("WO 1");
    }
}