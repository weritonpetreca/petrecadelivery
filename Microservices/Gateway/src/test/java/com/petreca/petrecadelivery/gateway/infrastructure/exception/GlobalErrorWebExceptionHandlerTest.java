package com.petreca.petrecadelivery.gateway.infrastructure.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Global Error Web Exception Handler Tests")
class GlobalErrorWebExceptionHandlerTest {

    @Mock
    private ErrorAttributes errorAttributes;

    @Mock
    private WebProperties webProperties;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ServerCodecConfigurer serverCodecConfigurer;

    private GlobalErrorWebExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(webProperties.getResources()).thenReturn(new WebProperties.Resources());
        when(applicationContext.getClassLoader()).thenReturn(this.getClass().getClassLoader());

        exceptionHandler = new GlobalErrorWebExceptionHandler(
                errorAttributes, webProperties, applicationContext, serverCodecConfigurer);
    }

    @Test
    @DisplayName("should return 502 Bad Gateway when upstream connection is refused")
    void shouldReturnBadGatewayOnConnectException() {
        ConnectException exception = new ConnectException("Connection refused");
        when(errorAttributes.getError(any())).thenReturn(exception);

        Map<String, Object> attributes = new HashMap<>();
        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class))).thenReturn(attributes);

        MockServerRequest request = MockServerRequest.builder().build();

        ServerResponse response = executeRoute(request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.statusCode());
        assertEquals(502, attributes.get("status"));
        assertEquals("Upstream service is unreachable. Connection Refused.", attributes.get("message"));
    }

    @Test
    @DisplayName("should return 503 Service Unavailable when Circuit Breaker is OPEN")
    void shouldReturnServiceUnavailableOnCallNotPermittedException() {
        CallNotPermittedException exception = mock(CallNotPermittedException.class);
        when(errorAttributes.getError(any())).thenReturn(exception);

        Map<String, Object> attributes = new HashMap<>();
        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class))).thenReturn(attributes);

        MockServerRequest request = MockServerRequest.builder().build();

        ServerResponse response = executeRoute(request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode());
        assertEquals(503, attributes.get("status"));
        assertEquals("Circuit Breaker is OPEN. Service is temporarily unavailable.", attributes.get("message"));
    }

    @Test
    @DisplayName("should return 500 Internal Server Error for generic unknown exceptions")
    void shouldReturnInternalServerErrorOnGenericException() {
        RuntimeException exception = new RuntimeException("A generic unknown error");
        when(errorAttributes.getError(any())).thenReturn(exception);

        Map<String, Object> attributes = new HashMap<>();
        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class))).thenReturn(attributes);

        MockServerRequest request = MockServerRequest.builder().build();

        ServerResponse response = executeRoute(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
    }

    private ServerResponse executeRoute(MockServerRequest request) {
        RouterFunction<ServerResponse> routerFunction = exceptionHandler.getRoutingFunction(errorAttributes);
        HandlerFunction<ServerResponse> handlerFunction = routerFunction.route(request).block();

        assertNotNull(handlerFunction, "Handler function should not be null for this route");

        ServerResponse response = handlerFunction.handle(request).block();
        assertNotNull(response, "Server response should not be null");

        return response;
    }
}
