package com.petreca.petrecadelivery.delivery.tracking.domain.model;

import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryFulfilledEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryPickedUpEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryPlacedEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários do Aggregate Root {@link Delivery}.
 *
 * <p>Sem Spring, sem banco de dados — apenas regras de negócio puras.
 * Cada método de domínio tem seus próprios cenários agrupados com @Nested.</p>
 */
@DisplayName("Delivery")
class DeliveryTest {

    /*
     * @Nested agrupa testes relacionados visualmente e no relatório.
     * Cada inner class representa um comportamento ou método sendo testado.
     * No relatório do JUnit aparece como:
     *
     * Delivery
     *   ├── draft()
     *   │     └── should initialize with safe default values
     *   ├── place()
     *   │     ├── should change status to WAITING_FOR_COURIER when valid
     *   │     ├── should register DeliveryPlacedEvent when placed
     *   │     ├── should throw when preparation details are missing
     *   │     └── should throw when status is not DRAFT
     *   └── ...
     */

    // ─────────────────────────────────────────────────────────────────
    // Helpers — evitam repetição nos testes
    // ─────────────────────────────────────────────────────────────────

    /**
     * Cria um {@link Delivery.PreparationDetails} válido para reusar nos testes.
     * Centralizado aqui para que uma mudança na estrutura afete só este método.
     */
    private Delivery.PreparationDetails createValidPreparationDetails() {
        ContactPoint sender = ContactPoint.builder()
                .zipCode("12345-678")
                .street("Sender Street")
                .number("123")
                .complement("Apt 1")
                .name("João Silva")
                .phone("(12) 93456-7890")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("87654-321")
                .street("Recipient Street")
                .number("456")
                .complement("Apt 2")
                .name("Maria Oliveira")
                .phone("(98) 98765-4321")
                .build();

        return Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("15.00"))
                .courierPayout(new BigDecimal("5.00"))
                .expectedDeliveryTime(Duration.ofHours(5))
                .build();
    }

    /**
     * Cria uma {@link Delivery} já preparada (com dados preenchidos),
     * pronta para ser publicada com place().
     */
    private Delivery createPreparedDelivery() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createValidPreparationDetails());
        delivery.addItem("Notebook", 1);
        return delivery;
    }

    // ─────────────────────────────────────────────────────────────────
    // draft()
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("draft()")
    class Draft {

        @Test
        @DisplayName("should initialize with DRAFT status and safe default values")
        void shouldInitializeWithSafeDefaults() {
            /*
             * Verifica que o factory method entrega um objeto sempre
             * em estado válido — nunca com nulos em campos críticos.
             * Isso protege contra NullPointerException em operações
             * subsequentes como totalCost.add(...).
             */
            Delivery delivery = Delivery.draft();

            assertThat(delivery.getId()).isNotNull();
            assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DRAFT);
            assertThat(delivery.getTotalItems()).isZero();
            assertThat(delivery.getTotalCost()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(delivery.getCourierPayout()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(delivery.getDistanceFee()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // place()
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("place()")
    class Place {

        @Test
        @DisplayName("should change status to WAITING_FOR_COURIER when delivery is fully prepared")
        void shouldChangeStatusToWaitingForCourier() {
            Delivery delivery = createPreparedDelivery();

            delivery.place();

            assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.WAITING_FOR_COURIER);
            assertThat(delivery.getPlacedAt()).isNotNull();
        }

        @Test
        @DisplayName("should register DeliveryPlacedEvent when placed successfully")
        void shouldRegisterDeliveryPlacedEvent() {
            /*
             * Domain Events são registrados via super.registerEvent()
             * e ficam acessíveis via domainEvents() até serem publicados
             * pelo Spring Data após o saveAndFlush().
             * Aqui verificamos que o evento certo foi registrado.
             */
            Delivery delivery = createPreparedDelivery();

            delivery.place();

            assertThat(delivery.domainEvents())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(DeliveryPlacedEvent.class);
        }

        @Test
        @DisplayName("should throw DomainException when preparation details are missing")
        void shouldThrowWhenPreparationDetailsAreMissing() {
            /*
             * Este teste cobre o bug que corrigimos em isFilled():
             * totalCost == ZERO não deve ser considerado preenchido.
             * Antes da correção, este cenário passaria sem lançar exceção
             * porque totalCost != null era sempre verdadeiro.
             */
            Delivery delivery = Delivery.draft(); // sem chamar editPreparationDetails()

            assertThatThrownBy(delivery::place)
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("incomplete");
        }

        @Test
        @DisplayName("should throw DomainException when status is not DRAFT")
        void shouldThrowWhenStatusIsNotDraft() {
            /*
             * Cobre o segundo guard em verifyIfCanBePlaced():
             * tentar publicar uma entrega que já foi publicada deve falhar.
             */
            Delivery delivery = createPreparedDelivery();
            delivery.place(); // primeira chamada — válida

            assertThatThrownBy(delivery::place) // segunda chamada — inválida
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("status");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // pickUp()
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("pickUp()")
    class PickUp {

        @Test
        @DisplayName("should change status to IN_TRANSIT and assign courierId")
        void shouldChangeStatusToInTransit() {
            Delivery delivery = createPreparedDelivery();
            delivery.place();

            UUID courierId = UUID.randomUUID();
            delivery.pickUp(courierId);

            assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
            assertThat(delivery.getCourierId()).isEqualTo(courierId);
            assertThat(delivery.getAssignedAt()).isNotNull();
        }

        @Test
        @DisplayName("should register DeliveryPickedUpEvent when picked up")
        void shouldRegisterDeliveryPickedUpEvent() {
            Delivery delivery = createPreparedDelivery();
            delivery.place();

            delivery.pickUp(UUID.randomUUID());

            assertThat(delivery.domainEvents())
                    .hasSize(2) // PlacedEvent + PickedUpEvent
                    .last()
                    .isInstanceOf(DeliveryPickedUpEvent.class);
        }

        @Test
        @DisplayName("should throw DomainException when status is not WAITING_FOR_COURIER")
        void shouldThrowWhenStatusIsNotWaitingForCourier() {
            /*
             * Testa a state machine: não é possível pular etapas.
             * DRAFT → IN_TRANSIT deve ser bloqueado.
             */
            Delivery delivery = createPreparedDelivery(); // status = DRAFT

            UUID courierId = UUID.randomUUID();

            assertThatThrownBy(() -> delivery.pickUp(courierId))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Invalid status transition");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // markAsDelivered()
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("markAsDelivered()")
    class MarkAsDelivered {

        @Test
        @DisplayName("should change status to DELIVERED and set fulfilledAt")
        void shouldChangeStatusToDelivered() {
            Delivery delivery = createPreparedDelivery();
            delivery.place();
            delivery.pickUp(UUID.randomUUID());

            delivery.markAsDelivered();

            assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
            assertThat(delivery.getFulfilledAt()).isNotNull();
        }

        @Test
        @DisplayName("should register DeliveryFulfilledEvent when delivered")
        void shouldRegisterDeliveryFulfilledEvent() {
            Delivery delivery = createPreparedDelivery();
            delivery.place();
            delivery.pickUp(UUID.randomUUID());

            delivery.markAsDelivered();

            assertThat(delivery.domainEvents())
                    .hasSize(3) // Placed + PickedUp + Fulfilled
                    .last()
                    .isInstanceOf(DeliveryFulfilledEvent.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // addItem() / removeItem() / changeItemQuantity()
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("item management")
    class ItemManagement {

        @Test
        @DisplayName("should recalculate totalItems when items are added")
        void shouldRecalculateTotalItemsOnAdd() {
            Delivery delivery = Delivery.draft();

            delivery.addItem("Notebook", 2);
            delivery.addItem("Mouse", 3);

            assertThat(delivery.getTotalItems()).isEqualTo(5);
        }

        @Test
        @DisplayName("should recalculate totalItems when an item is removed")
        void shouldRecalculateTotalItemsOnRemove() {
            Delivery delivery = Delivery.draft();
            UUID itemId = delivery.addItem("Notebook", 2);
            delivery.addItem("Mouse", 3);

            delivery.removeItem(itemId);

            assertThat(delivery.getTotalItems()).isEqualTo(3);
        }

        @Test
        @DisplayName("should recalculate totalItems when item quantity is changed")
        void shouldRecalculateTotalItemsOnQuantityChange() {
            Delivery delivery = Delivery.draft();
            UUID itemId = delivery.addItem("Notebook", 2);

            delivery.changeItemQuantity(itemId, 5);

            assertThat(delivery.getTotalItems()).isEqualTo(5);
        }

        @Test
        @DisplayName("should return unmodifiable list of items")
        void shouldReturnUnmodifiableItems() {
            /*
             * getItems() retorna Collections.unmodifiableList().
             * Qualquer tentativa de modificar a lista externamente
             * deve lançar UnsupportedOperationException — isso garante
             * que o estado interno só muda via métodos de domínio.
             */
            Delivery delivery = Delivery.draft();
            delivery.addItem("Notebook", 1);

            List<Item> items = delivery.getItems();

            assertThatThrownBy(items::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}