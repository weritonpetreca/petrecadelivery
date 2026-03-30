package com.petreca.petrecadelivery.delivery.tracking.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.petreca.petrecadelivery.delivery.tracking.domain.model.DeliveryStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários da state machine {@link DeliveryStatus}.
 *
 * <p>Cobre todas as transições válidas e inválidas entre os estados
 * do ciclo de vida de uma entrega, garantindo que as regras de negócio
 * da máquina de estados sejam respeitadas independentemente de quem
 * chame {@code canChangeTo()}.</p>
 *
 * <p>Estes testes são puramente unitários — sem Spring, sem banco,
 * sem infraestrutura. Apenas lógica de negócio pura.</p>
 */
@DisplayName("DeliveryStatus state machine")
class DeliveryStatusTest {

    // ─────────────────────────────────────────────────────────────────
    // Transições VÁLIDAS
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("valid transitions")
    class ValidTransitions {

        /*
         * @ParameterizedTest executa o mesmo teste para cada conjunto
         * de argumentos fornecido pelo @MethodSource.
         *
         * Vantagem: ao adicionar um novo status no futuro, basta
         * adicionar uma linha no Stream — o teste já cobre o novo caso.
         */
        @ParameterizedTest(name = "{0} → {1} should be allowed")
        @MethodSource("validTransitionProvider")
        @DisplayName("should allow")
        void shouldAllowValidTransition(DeliveryStatus from, DeliveryStatus to) {
            assertThat(from.canChangeTo(to))
                    .as("Expected transition from %s to %s to be VALID", from, to)
                    .isTrue();
        }

        /**
         * Fonte de dados para transições válidas.
         * O nome do método deve corresponder ao valor em @MethodSource.
         *
         * Fluxo válido completo:
         * DRAFT → WAITING_FOR_COURIER → IN_TRANSIT → DELIVERED
         */
        static Stream<Arguments> validTransitionProvider() {
            return Stream.of(
                    Arguments.of(DRAFT,               WAITING_FOR_COURIER),
                    Arguments.of(WAITING_FOR_COURIER, IN_TRANSIT),
                    Arguments.of(IN_TRANSIT,          DELIVERED)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Transições INVÁLIDAS
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("invalid transitions")
    class InvalidTransitions {

        @ParameterizedTest(name = "{0} → {1} should be rejected")
        @MethodSource("invalidTransitionProvider")
        @DisplayName("should reject")
        void shouldRejectInvalidTransition(DeliveryStatus from, DeliveryStatus to) {
            assertThat(from.canChangeTo(to))
                    .as("Expected transition from %s to %s to be INVALID", from, to)
                    .isFalse();
        }

        /**
         * Fonte de dados para transições inválidas.
         *
         * Cobre três categorias de transições proibidas:
         *   1. Pular etapas (ex: DRAFT → IN_TRANSIT)
         *   2. Voltar atrás (ex: IN_TRANSIT → DRAFT)
         *   3. Estado final (DELIVERED não vai a lugar nenhum)
         */
        static Stream<Arguments> invalidTransitionProvider() {
            return Stream.of(
                    // DRAFT não pode pular etapas
                    Arguments.of(DRAFT,               IN_TRANSIT),
                    Arguments.of(DRAFT,               DELIVERED),

                    // WAITING_FOR_COURIER não pode voltar ou pular
                    Arguments.of(WAITING_FOR_COURIER, DRAFT),
                    Arguments.of(WAITING_FOR_COURIER, DELIVERED),

                    // IN_TRANSIT não pode voltar
                    Arguments.of(IN_TRANSIT,          DRAFT),
                    Arguments.of(IN_TRANSIT,          WAITING_FOR_COURIER),

                    // DELIVERED é estado final — nenhuma transição é válida
                    Arguments.of(DELIVERED,           DRAFT),
                    Arguments.of(DELIVERED,           WAITING_FOR_COURIER),
                    Arguments.of(DELIVERED,           IN_TRANSIT)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Auto-transição
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("self-transitions")
    class SelfTransitions {

        /*
         * Um status não deve conseguir transicionar para si mesmo.
         * Ex: DRAFT → DRAFT não faz sentido no negócio.
         * Testamos isso separadamente para deixar a intenção clara.
         */
        @ParameterizedTest(name = "{0} → {0} should be rejected")
        @MethodSource("allStatuses")
        @DisplayName("should reject self-transition for all statuses")
        void shouldRejectSelfTransition(DeliveryStatus status) {
            assertThat(status.canChangeTo(status))
                    .as("Expected self-transition of %s to be INVALID", status)
                    .isFalse();
        }

        static Stream<Arguments> allStatuses() {
            return Stream.of(DeliveryStatus.values())
                    .map(Arguments::of);
        }
    }
}