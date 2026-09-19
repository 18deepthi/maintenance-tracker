package com.maintenance.tracker.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.maintenance.tracker.model.WorkOrder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class WorkOrderOptimisticLockingTest {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("Concurrent update on stale version throws ObjectOptimisticLockingFailureException")
    void concurrentUpdate_onStaleVersion_shouldThrowOptimisticLockingFailureException() {
        // Step 1: Persist initial entity and commit to DB
        EntityManager emSetup = entityManagerFactory.createEntityManager();
        emSetup.getTransaction().begin();
        WorkOrder initial = new WorkOrder("Initial Title", "Desc", "Eq", "E-1", "Loc", "admin", null);
        emSetup.persist(initial);
        emSetup.getTransaction().commit();
        Long id = initial.getId();
        emSetup.close();

        // Step 2: Load the same row in two separate persistence contexts
        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();

        WorkOrder copy1 = em1.find(WorkOrder.class, id);
        WorkOrder copy2 = em2.find(WorkOrder.class, id);

        assertThat(copy1.getVersion()).isEqualTo(copy2.getVersion());

        // Step 3: Update and commit in context 1 (increments version in database)
        em1.getTransaction().begin();
        copy1.setTitle("Updated by Context 1");
        em1.getTransaction().commit();
        em1.close();

        // Step 4: Update stale copy in context 2 via repository (assert ObjectOptimisticLockingFailureException)
        copy2.setTitle("Conflicting Update by Context 2");

        assertThatThrownBy(() -> workOrderRepository.saveAndFlush(copy2))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        em2.close();
    }
}