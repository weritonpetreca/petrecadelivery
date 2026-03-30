package com.petreca.petrecadelivery.delivery.tracking.infrastructure.http.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("Courier Payout Calculation HTTP Wrapper Tests")
public class CourierPayoutCalculationServiceHttpImplTest {

    @Mock
    private CourierAPIClient courierAPIClient;

    @InjectMocks
    private CourierPayoutCalculationServiceHttpImpl payoutService;

    @Nested
    @DisplayName("When calculating payout via external API")
    class CalculatePayout {

        @Test
        @DisplayName("should return payout fee on sucessful HTTP response")
        void shouldReturnPayoutFee() {
            Double distance = 10.0;
            BigDecimal expectedPayout = new BigDecimal("15.50");

            CourierPayoutResultModel mockResult = mock(CourierPayoutResultModel.class);
            given(mockResult.getPayoutFee()).willReturn(expectedPayout);

            given(courierAPIClient.payoutCalculation(any(CourierPayoutCalculationInput.class)))
                    .willReturn(mockResult);

            BigDecimal result = payoutService.calculatePayout(distance);

            assertThat(result).isEqualTo(expectedPayout);
        }

        @Test
        @DisplayName("should map ResourceAccessException (Timeout) to GatewayTimeoutException")
        void shouldMapResourceAccessException() {
            given(courierAPIClient.payoutCalculation(any()))
                    .willThrow(new ResourceAccessException("Connection timed out"));

            assertThatThrownBy(() -> payoutService.calculatePayout(10.0))
                    .isInstanceOf(GatewayTimeoutException.class);
        }

        @Test
        @DisplayName("should map CallNotPermittedException (Open Circuit) to BadGatewayException")
        void shouldMapCallNotPermittedException() {
            CallNotPermittedException mockException = mock(CallNotPermittedException.class);
            given(courierAPIClient.payoutCalculation(any()))
                    .willThrow(mockException);

            assertThatThrownBy(()-> payoutService.calculatePayout(10.0))
                    .isInstanceOf(BadGatewayException.class);
        }
        @Test
        @DisplayName("should map HttpServerErrorException (500 Error) to BadGatewayException")
        void shouldMapHttpServerErrorException() {
            given(courierAPIClient.payoutCalculation(any()))
                    .willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

            assertThatThrownBy(()-> payoutService.calculatePayout(10.0))
                    .isInstanceOf(BadGatewayException.class);
        }
    }
}
