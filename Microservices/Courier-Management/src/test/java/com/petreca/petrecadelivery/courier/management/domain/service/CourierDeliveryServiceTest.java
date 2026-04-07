package com.petreca.petrecadelivery.courier.management.domain.service;

import com.petreca.petrecadelivery.courier.management.domain.exception.DomainException;
import com.petreca.petrecadelivery.courier.management.domain.exception.NoCouriersAvailableException;
import com.petreca.petrecadelivery.courier.management.domain.model.Courier;
import com.petreca.petrecadelivery.courier.management.domain.repository.CourierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("Courier Delivery Service Tests")
public class CourierDeliveryServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierDeliveryService courierDeliveryService;

    @Nested
    @DisplayName("When assigning a delivery")
    class AssignDelivery {

        @Test
        @DisplayName("should find available courier, assign, and save")
        void shouldAssignSuccessfully() {
            UUID deliveryId = UUID.randomUUID();
            UUID courierId = UUID.randomUUID();
            Courier mockCourier = mock(Courier.class);

            given(courierRepository.findById(courierId))
                    .willReturn(Optional.of(mockCourier));

            courierDeliveryService.assign(deliveryId, courierId);

            then(mockCourier).should().assign(deliveryId);
            then(courierRepository).should().saveAndFlush(mockCourier);
        }

        @Test
        @DisplayName("should throw NoCourierAvailableException if repository returns empty")
        void shouldThrowWhenNoCouriers() {
            UUID deliveryId = UUID.randomUUID();
            UUID courierId = UUID.randomUUID();

            given(courierRepository.findById(courierId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> courierDeliveryService.assign(deliveryId, courierId))
                    .isInstanceOf(NoCouriersAvailableException.class)
                    .hasMessageContaining("Courier not found for assigment");
        }
    }

    @Nested
    @DisplayName("When fulfilling a delivery")
    class FulfillDelivery {

        @Test
        @DisplayName("should find assigned courier, fulfill, and save")
        void shouldFulfillSuccessfully() {
            UUID deliveryId = UUID.randomUUID();
            Courier mockCourier = mock(Courier.class);

            given(courierRepository.findByPendingDeliveries_id(deliveryId))
                    .willReturn(Optional.of(mockCourier));

            courierDeliveryService.fulfill(deliveryId);

            then(mockCourier).should().fulfill(deliveryId);
            then(courierRepository).should().saveAndFlush(mockCourier);
        }

        @Test
        @DisplayName("should throw DomainException if delivery is not found in any courier's pending list")
        void shouldThrowWhenDeliveryNotFound() {
            UUID deliveryId = UUID.randomUUID();
            given(courierRepository.findByPendingDeliveries_id(deliveryId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> courierDeliveryService.fulfill(deliveryId))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("No courier found");
        }
    }
}
