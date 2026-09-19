package com.maintenance.tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maintenance.tracker.dto.CreateWorkOrderRequest;
import com.maintenance.tracker.dto.PageResponse;
import com.maintenance.tracker.dto.UpdateWorkOrderDetailsRequest;
import com.maintenance.tracker.dto.WorkOrderResponse;
import com.maintenance.tracker.exception.InvalidWorkOrderStateException;
import com.maintenance.tracker.exception.ResourceNotFoundException;
import com.maintenance.tracker.model.WorkOrder;
import com.maintenance.tracker.model.WorkOrderStatus;
import com.maintenance.tracker.repository.WorkOrderRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @InjectMocks
    private WorkOrderService workOrderService;

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
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse updatedResponse = workOrderService.updateWorkOrderDetails(1L, updateRequest);

        assertThat(updatedResponse.getTitle()).isEqualTo("New Title");
        assertThat(updatedResponse.getDescription()).isEqualTo("New Desc");
        assertThat(updatedResponse.getEquipmentName()).isEqualTo("New Eq");
        assertThat(updatedResponse.getEquipmentId()).isEqualTo("E-new");
        assertThat(updatedResponse.getLocation()).isEqualTo("Loc New");
        verify(workOrderRepository).save(existing);
    }

    @ParameterizedTest
    @EnumSource(value = WorkOrderStatus.class, names = {"IN_PROGRESS", "COMPLETED", "CLOSED"})
    @DisplayName("updateWorkOrderDetails throws InvalidWorkOrderStateException when not in OPEN status (FR-8)")
    void updateWorkOrderDetails_whenStatusIsNotOpen_shouldThrowInvalidWorkOrderStateException(WorkOrderStatus nonOpenStatus) {
        WorkOrder existing = new WorkOrder("Title", "Desc", "Eq", "E-1", "Loc", "sup1", "eng1");
        existing.setId(2L);
        existing.setStatus(nonOpenStatus);

        UpdateWorkOrderDetailsRequest updateRequest = new UpdateWorkOrderDetailsRequest(
                "Updated Title", "Updated Desc", "Updated Eq", "E-updated", "Loc Updated"
        );

        when(workOrderRepository.findById(2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> workOrderService.updateWorkOrderDetails(2L, updateRequest))
                .isInstanceOf(InvalidWorkOrderStateException.class)
                .hasMessageContaining("FR-8")
                .hasMessageContaining(nonOpenStatus.name());

        verify(workOrderRepository, never()).save(any(WorkOrder.class));
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
}