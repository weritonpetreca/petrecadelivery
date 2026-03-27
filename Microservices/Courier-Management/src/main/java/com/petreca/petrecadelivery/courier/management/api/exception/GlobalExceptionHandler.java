package com.petreca.petrecadelivery.courier.management.api.exception;


import com.petreca.petrecadelivery.courier.management.domain.exception.DomainException;
import com.petreca.petrecadelivery.courier.management.domain.exception.NoCouriersAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Centralizador global de tratamento de exceções para o Courier Management.
 *
 * <p>Separa a responsabilidade de converter exceções de domínio em respostas HTTP
 * da lógica de negócio. Sem isso, cada controller precisaria tratar suas próprias
 * exceções, espalhando código de infraestrutura HTTP pelo domínio.</p>
 *
 * <p>Usa {@link ProblemDetail} — padrão RFC 9457 (Problem Details for HTTP APIs)
 * já suportado nativamente pelo Spring Boot 3. Fornece respostas de erro
 * padronizadas e legíveis por qualquer cliente HTTP.</p>
 *
 * <p>Exemplo de resposta gerada:</p>
 * <pre>{@code
 * {
 *   "type": "about:blank",
 *   "title": "Not Found",
 *   "status": 404,
 *   "detail": "Courier not found: 123e4567-e89b-12d3-a456-426614174000",
 *   "instance": "/api/v1/couriers/123e4567-e89b-12d3-a456-426614174000"
 * }
 * }</pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata {@link DomainException} mapeando para HTTP 404 Not Found.
     *
     * <p>Convenção adotada: DomainException no contexto de consultas
     * indica que o recurso não foi encontrado. Se futuramente houver
     * necessidade de distinguir tipos de erro de domínio, criar
     * subclasses específicas (ex: CourierNotFoundException).</p>
     *
     * @param ex exceção de domínio capturada
     * @return resposta padronizada no formato Problem Detail (RFC 9457)
     */
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(NoCouriersAvailableException.class)
    public ProblemDetail handleNoCouriersAvailableException(NoCouriersAvailableException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }


}
