package com.petreca.petrecadelivery.gateway.infrastructure.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties,
                                          ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer){
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(serverCodecConfigurer.getWriters());
        this.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (error instanceof ConnectException || error.getCause() instanceof ConnectException) {
            status = HttpStatus.BAD_GATEWAY;
            errorPropertiesMap.put("status", status.value());
            errorPropertiesMap.put("error", status.getReasonPhrase());
            errorPropertiesMap.put("message", "Upstream service is unreachable. Connection Refused.");
        } else if (error.getClass().getSimpleName().equals("CallNotPermittedException")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorPropertiesMap.put("status", status.value());
            errorPropertiesMap.put("error", status.getReasonPhrase());
            errorPropertiesMap.put("message", "Circuit Breaker is OPEN. Service is temporarily unavailable.");
        }

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}
