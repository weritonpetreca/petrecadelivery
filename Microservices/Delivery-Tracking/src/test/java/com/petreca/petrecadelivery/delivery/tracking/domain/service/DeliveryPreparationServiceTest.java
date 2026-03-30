package com.petreca.petrecadelivery.delivery.tracking.domain.service;

import com.petreca.petrecadelivery.delivery.tracking.api.model.ContactPointInput;
import com.petreca.petrecadelivery.delivery.tracking.api.model.DeliveryInput;
import com.petreca.petrecadelivery.delivery.tracking.api.model.ItemInput;
import com.petreca.petrecadelivery.delivery.tracking.domain.exception.DomainException;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.ContactPoint;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.Delivery;
import com.petreca.petrecadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("Delivery Preparation Service")
public class DeliveryPreparationServiceTest {

    @Mock private DeliveryRepository repository;
    @Mock private DeliveryTimeEstimationService timeService;
    @Mock private CourierPayoutCalculationService payoutService;
    @InjectMocks private DeliveryPreparationService service;

    @Captor private ArgumentCaptor<Delivery> deliveryCaptor;

    private DeliveryInput createValidInput() {
        ContactPointInput sender = new ContactPointInput(
                "12345-000", "Street", "10", "Apt", "Sender", "119999");
        ContactPointInput recipient = new ContactPointInput(
                "54321-000", "Avenue", "20", "", "Recipient", "118888");
        List<ItemInput> items = List.of(new ItemInput("Silver Sword", 1));
        return new DeliveryInput(sender, recipient, items);
    }

    private void mockTimeAndPayout(double distanceInKm, int minutes, BigDecimal payout) {
        DeliveryEstimate mockEstimate = mock(DeliveryEstimate.class);
        given(mockEstimate.getDistanceInKm()).willReturn(distanceInKm);
        given(mockEstimate.getEstimatedTime()).willReturn(Duration.ofMinutes(minutes));

        given(timeService.estimate(any(ContactPoint.class), any(ContactPoint.class)))
                .willReturn(mockEstimate);

        given(payoutService.calculatePayout(distanceInKm))
                .willReturn(payout);
    }

    @Nested
    @DisplayName("When drafting a new delivery")
    class DraftOperation {

        @Test
        @DisplayName("should correctly map DTO and calculate inital fees")
        void shouldDraftAndSave() {

            DeliveryInput input = createValidInput();
            mockTimeAndPayout(10.0, 45, BigDecimal.valueOf(15));

            given(repository.saveAndFlush(any(Delivery.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            Delivery result = service.draft(input);

            assertThat(result).isNotNull();
            assertThat(result.getTotalItems()).isEqualTo(1);
            assertThat(result.getCourierPayout()).isEqualByComparingTo(BigDecimal.valueOf(15));
            assertThat(result.getDistanceFee()).isEqualByComparingTo(BigDecimal.valueOf(30));

            then(repository).should().saveAndFlush(deliveryCaptor.capture());
            assertThat(deliveryCaptor.getValue().getItems().getFirst().getName()).isEqualTo("Silver Sword");
        }
    }

    @Nested
    @DisplayName("When editing an existing delivery")
    class EditOperation {

        @Test
        @DisplayName("should update preparation details and clear old items")
        void shouldEditAndSave() {
            UUID deliveryId = UUID.randomUUID();
            Delivery existingDelivery = Delivery.draft();
            DeliveryInput input = createValidInput();

            mockTimeAndPayout(5.0, 20, BigDecimal.valueOf(8.00));

            given(repository.findById(deliveryId)).willReturn(Optional.of(existingDelivery));
            given(repository.saveAndFlush(existingDelivery)).willReturn(existingDelivery);

            Delivery result = service.edit(deliveryId, input);

            assertThat(result.getCourierPayout()).isEqualByComparingTo(BigDecimal.valueOf(8.00));
            assertThat(result.getDistanceFee()).isEqualByComparingTo(BigDecimal.valueOf(15.00));

            then(repository).should().saveAndFlush(existingDelivery);
        }

        @Test
        @DisplayName("should throw DomainException when trying to edit a non-existent delivery")
        void shouldThrowIfNotFoundDuringEdit() {
            UUID deliveryId = UUID.randomUUID();
            DeliveryInput input = createValidInput();

            given(repository.findById(deliveryId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.edit(deliveryId, input))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("not found");
        }
    }
}
