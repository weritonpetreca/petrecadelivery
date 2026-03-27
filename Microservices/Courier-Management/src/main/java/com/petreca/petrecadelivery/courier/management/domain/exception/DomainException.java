package com.petreca.petrecadelivery.courier.management.domain.exception;

/**
 * Exceção de domínio do microsserviço Courier Management.
 *
 * <p>Representa violações das regras de negócio relacionadas
 * a entregadores — estado inválido, entidade não encontrada,
 * ou operação não permitida no contexto atual.</p>
 *
 * <p>Por ser uma {@link RuntimeException}, não precisa ser declarada
 * na assinatura dos métodos, mantendo o código limpo. O tratamento
 * é feito de forma centralizada pelo exception handler global.</p>
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) { super(message, cause); }
}
