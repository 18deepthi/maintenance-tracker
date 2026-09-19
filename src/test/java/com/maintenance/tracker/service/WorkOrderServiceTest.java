package com.maintenance.tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maintenance.tracker.dto.CreateWorkOrderRequest;
import com.maintenance.tracker.dto.PageResponse;
import com.maintenance.tracker.dto.UpdateAssignmentRequest;
import com.maintenance.tracker.dto.UpdateStatusRequest;
import com.maintenance.tracker.dto.UpdateWorkOrderDetailsRequest;
import com.maintenance.tracker.dto.WorkOrderFilterParams;
import com.maintenance.tracker.dto.WorkOrderResponse;
import com.maintenance.tracker.exception.InvalidSortFieldException;
import com.maintenance.tracker.exception.InvalidWorkOrderStateException;
import com.maintenance.tracker.exception.ResourceNotFoundException;
import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import com.maintenance.tracker.repository.WorkOrderRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @InjectMocks
    private WorkOrderService workOrderService;

    static Stream<Arguments> allStatusPairs() {
        List<Arguments> argumentsList = new ArrayList<>();
        for (WorkOrderStatus current : WorkOrderStatus.values()) {
            for (WorkOrderStatus target : WorkOrderStatus.values()) {
                boolean shouldSucceed = (current == WorkOrderStatus.OPEN && target == WorkOrderStatus.IN_PROGRESS)
                        || (current == WorkOrderStatus.IN_PROGRESS && target == WorkOrderStatus.COMPLETED)
                        || (current == WorkOrderStatus.COMPLETED && target == WorkOrderStatus.CLOSED);
                argumentsList.add(Arguments.of(current, target, shouldSucceed));
            }
        }
        return argumentsList.stream();
    }

    @Test
    @DisplayName("createWorkOrder saves order and returns OPEN status response")
    void createWorkOrder_shouldSaveAndReturnOpenResponse() {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest(
                "Inspect Transformer Bushing",
                "Periodic inspection",
                "Transformer T-1",
                "EQ-TR-01",
                "Substation 1",
                "supervisor_1",
                "engineer_a"
        );

        WorkOrder savedOrder = new WorkOrder(
                request.getTitle(),
                request.getDescription(),
                request.getEquipmentName(),
                request.getEquipmentId(),
                request.getLocation(),
                request.getCreatedBy(),
                request.getAssignedTo()
        );
        savedOrder.setId(10L);
        savedOrder.setStatus(WorkOrderStatus.OPEN);
        savedOrder.setCreatedAt(Instant.now());
        savedOrder.setUpdatedAt(Instant.now());

        when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(savedOrder);

        WorkOrderResponse response = workOrderService.createWorkOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("Inspect Transformer Bushing");
        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.OPEN);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    @DisplayName("getWorkOrderById returns mapped response when order exists")
    void getWorkOrderById_whenFound_shouldReturnResponse() {
        WorkOrder order = new WorkOrder("Title", "Desc", "Equip", "E-1", "Loc", "admin", null);
        order.setId(5L);
        order.setStatus(WorkOrderStatus.OPEN);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        when(workOrderRepository.findById(5L)).thenReturn(Optional.of(order));

        WorkOrderResponse response = workOrderService.getWorkOrderById(5L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getTitle()).isEqualTo("Title");
    }

    @Test
    @DisplayName("getWorkOrderById throws ResourceNotFoundException when ID does not exist")
    void getWorkOrderById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(workOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.getWorkOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("listWorkOrders clamps page size to MAX_PAGE_SIZE (100) and returns PageResponse")
    void listWorkOrders_shouldClampPageSizeTo100AndReturnPageResponse() {
        WorkOrder order = new WorkOrder("Title", "Desc", "Equip", "E-1", "Loc", "admin", null);
        order.setId(1L);
        order.setStatus(WorkOrderStatus.OPEN);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        Page<WorkOrder> page = new PageImpl<>(List.of(order), PageRequest.of(0, 100), 1);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(workOrderRepository.findAll(captor.capture())).thenReturn(page);

        PageResponse<WorkOrderResponse> result = workOrderService.listWorkOrders(PageRequest.of(0, 250));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("listWorkOrders with filter parameters invokes specification search")
    void listWorkOrders_withFilters_shouldPassSpecificationToRepository() {
        WorkOrderFilterParams filters = new WorkOrderFilterParams(WorkOrderStatus.OPEN, "eng1", "Pump", "EQ-1");
        Page<WorkOrder> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(workOrderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<WorkOrderResponse> result = workOrderService.listWorkOrders(filters, PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        verify(workOrderRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("updateWorkOrderDetails updates details when work order is in OPEN status (FR-8)")
    void updateWorkOrderDetails_whenStatusIsOpen_shouldUpdateAndReturnResponse() {
        WorkOrder existing = new WorkOrder("Old Title", "Old Desc", "Old Eq", "E-old", "Loc Old", "sup1", null);
        existing.setId(1L);
        existing.setStatus(WorkOrderStatus.OPEN);
        existing.setCreatedAt(Instant.now());
        existing.setUpdatedAt(Instant.now());

        UpdateWorkOrderDetailsRequest updateRequest = new UpdateWorkOrderDetailsRequest(
                "New Title", "New Desc", "New Eq", "E-new", "Loc New"
        );

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(workOrderRepository.saveAndFlush(any(WorkOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse updatedResponse = workOrderService.updateWorkOrderDetails(1L, updateRequest);

        assertThat(updatedResponse.getTitle()).isEqualTo("New Title");
        assertThat(updatedResponse.getDescription()).isEqualTo("New Desc");
        assertThat(updatedResponse.getEquipmentName()).isEqualTo("New Eq");
        assertThat(updatedResponse.getEquipmentId()).isEqualTo("E-new");
        assertThat(updatedResponse.getLocation()).isEqualTo("Loc New");
        verify(workOrderRepository).saveAndFlush(existing);
    }

    @ParameterizedTest
    @EnumSource(value = WorkOrderStatus.class, names = {"IN_PROGRESS", "COMPLETED", "CLOSED"})
    @DisplayName("updateWorkOrderDetails throws InvalidWorkOrderStateException when not in OPEN status (FR-8)")
    void updateWorkOrderDetails_whenStatusIsNotOpen_shouldThrowInvalidWorkOrderStateException(
            WorkOrderStatus nonOpenStatus) {
        WorkOrder existing = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", "eng1");
        existing.setId(2L);
        existing.setStatus(nonOpenStatus);

        UpdateWorkOrderDetailsRequest updateRequest = new UpdateWorkOrderDetailsRequest(
                "Updated Title", "Updated Desc", "Updated Eq", "E-updated", "Loc Updated"
        );

        when(workOrderRepository.findById(2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> workOrderService.updateWorkOrderDetails(2L, updateRequest))
                .isInstanceOf(InvalidWorkOrderStateException.class)
                .hasMessageContaining("Updates are permitted only while status is OPEN")
                .hasMessageContaining(nonOpenStatus.name());

        verify(workOrderRepository, never()).saveAndFlush(any(WorkOrder.class));
    }

    @Test
    @DisplayName("updateWorkOrderDetails throws ResourceNotFoundException when work order does not exist")
    void updateWorkOrderDetails_whenNotFound_shouldThrowResourceNotFoundException() {
        UpdateWorkOrderDetailsRequest updateRequest = new UpdateWorkOrderDetailsRequest(
                "Title", "Desc", "Eq", "E-1", "Loc"
        );

        when(workOrderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.updateWorkOrderDetails(404L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    // 16-pair exhaustive parameterized matrix test (FR-3, FR-4)
    @ParameterizedTest(name = "Transition from {0} to {1} should succeed: {2}")
    @MethodSource("allStatusPairs")
    @DisplayName("16-pair status transition matrix: "
            + "only OPEN->IN_PROGRESS, IN_PROGRESS->COMPLETED, COMPLETED->CLOSED succeed")
    void updateWorkOrderStatus_allStatusPairs_shouldEnforceStrictOneWayTransitions(
            WorkOrderStatus currentStatus, WorkOrderStatus targetStatus, boolean shouldSucceed) {

        WorkOrder order = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", "engineer_1");
        order.setId(1L);
        order.setStatus(currentStatus);

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        if (shouldSucceed) {
            when(workOrderRepository.saveAndFlush(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
            WorkOrderResponse response = workOrderService.updateWorkOrderStatus(
                    1L, new UpdateStatusRequest(targetStatus));
            assertThat(response.getStatus()).isEqualTo(targetStatus);
            verify(workOrderRepository).saveAndFlush(order);
        } else {
            assertThatThrownBy(() -> workOrderService.updateWorkOrderStatus(1L, new UpdateStatusRequest(targetStatus)))
                    .isInstanceOf(InvalidWorkOrderStateException.class)
                    .hasMessageContaining("strictly one step");
            verify(workOrderRepository, never()).saveAndFlush(any(WorkOrder.class));
        }
    }

    @Test
    @DisplayName("Transitioning to IN_PROGRESS without an assignee throws InvalidWorkOrderStateException (FR-5)")
    void updateWorkOrderStatus_toInProgressWithoutAssignee_shouldThrowInvalidWorkOrderStateException() {
        WorkOrder unassignedOrder = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", null);
        unassignedOrder.setId(1L);
        unassignedOrder.setStatus(WorkOrderStatus.OPEN);

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(unassignedOrder));

        assertThatThrownBy(() -> workOrderService.updateWorkOrderStatus(
                1L, new UpdateStatusRequest(WorkOrderStatus.IN_PROGRESS)))
                .isInstanceOf(InvalidWorkOrderStateException.class)
                .hasMessageContaining("without an assigned engineer");

        verify(workOrderRepository, never()).saveAndFlush(any(WorkOrder.class));
    }

    @Test
    @DisplayName("updateWorkOrderStatus throws ResourceNotFoundException when order does not exist")
    void updateWorkOrderStatus_whenNotFound_shouldThrowResourceNotFoundException() {
        when(workOrderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.updateWorkOrderStatus(
                404L, new UpdateStatusRequest(WorkOrderStatus.IN_PROGRESS)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    // Assignment rule tests (FR-5)
    @Test
    @DisplayName("Assigning an OPEN work order preserves OPEN status and sets trimmed assignee")
    void updateAssignment_whenOpen_shouldPreserveOpenStatusAndTrim() {
        WorkOrder order = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", null);
        order.setId(1L);
        order.setStatus(WorkOrderStatus.OPEN);

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(workOrderRepository.saveAndFlush(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkOrderResponse response = workOrderService.updateAssignment(
                1L, new UpdateAssignmentRequest("  engineer_bob  "));

        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.OPEN);
        assertThat(response.getAssignedTo()).isEqualTo("engineer_bob");
        verify(workOrderRepository).saveAndFlush(order);
    }

    @Test
    @DisplayName("Reassigning an IN_PROGRESS work order preserves IN_PROGRESS status")
    void updateAssignment_whenInProgress_shouldPreserveInProgressStatus() {
        WorkOrder order = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", "engineer_bob");
        order.setId(1L);
        order.setStatus(WorkOrderStatus.IN_PROGRESS);

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(workOrderRepository.saveAndFlush(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkOrderResponse response = workOrderService.updateAssignment(
                1L, new UpdateAssignmentRequest("engineer_alice"));

        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
        assertThat(response.getAssignedTo()).isEqualTo("engineer_alice");
    }

    @Test
    @DisplayName("Unassigning with null or whitespace is permitted when status is OPEN")
    void updateAssignment_unassignWhenOpen_shouldSetAssigneeToNull() {
        WorkOrder order = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", "engineer_bob");
        order.setId(1L);
        order.setStatus(WorkOrderStatus.OPEN);

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(workOrderRepository.saveAndFlush(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkOrderResponse response = workOrderService.updateAssignment(1L, new UpdateAssignmentRequest("   "));

        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.OPEN);
        assertThat(response.getAssignedTo()).isNull();
    }

    @Test
    @DisplayName("Unassigning while IN_PROGRESS throws InvalidWorkOrderStateException (FR-5)")
    void updateAssignment_unassignWhenInProgress_shouldThrowInvalidWorkOrderStateException() {
        WorkOrder order = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", "engineer_bob");
        order.setId(1L);
        order.setStatus(WorkOrderStatus.IN_PROGRESS);

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> workOrderService.updateAssignment(1L, new UpdateAssignmentRequest("")))
                .isInstanceOf(InvalidWorkOrderStateException.class)
                .hasMessageContaining("unassigned while in OPEN status");

        verify(workOrderRepository, never()).saveAndFlush(any(WorkOrder.class));
    }

    @ParameterizedTest
    @EnumSource(value = WorkOrderStatus.class, names = {"COMPLETED", "CLOSED"})
    @DisplayName("Modifying assignment in COMPLETED or CLOSED throws InvalidWorkOrderStateException (FR-5)")
    void updateAssignment_whenCompletedOrClosed_shouldThrowInvalidWorkOrderStateException(
            WorkOrderStatus terminalStatus) {
        WorkOrder order = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", "engineer_bob");
        order.setId(1L);
        order.setStatus(terminalStatus);

        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> workOrderService.updateAssignment(1L, new UpdateAssignmentRequest("engineer_charlie")))
                .isInstanceOf(InvalidWorkOrderStateException.class)
                .hasMessageContaining("not permitted once COMPLETED or CLOSED");

        verify(workOrderRepository, never()).saveAndFlush(any(WorkOrder.class));
    }

    @Test
    @DisplayName("updateAssignment throws ResourceNotFoundException when work order does not exist")
    void updateAssignment_whenNotFound_shouldThrowResourceNotFoundException() {
        when(workOrderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.updateAssignment(
                404L, new UpdateAssignmentRequest("engineer_charlie")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("listWorkOrders throws InvalidSortFieldException when sort field is not whitelisted")
    void listWorkOrders_whenInvalidSortField_shouldThrowInvalidSortFieldException() {
        Pageable pageable = PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("banana"));

        assertThatThrownBy(() -> workOrderService.listWorkOrders(null, pageable))
                .isInstanceOf(InvalidSortFieldException.class)
                .hasMessageContaining("banana");
    }
}
