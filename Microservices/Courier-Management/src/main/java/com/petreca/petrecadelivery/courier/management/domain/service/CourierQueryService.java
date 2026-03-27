package com.petreca.petrecadelivery.courier.management.domain.service;

import com.petreca.petrecadelivery.courier.management.domain.exception.DomainException;
import com.petreca.petrecadelivery.courier.management.domain.model.Courier;
import com.petreca.petrecadelivery.courier.management.domain.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pelas operações de consulta de entregadores.
 *
 * <p>Separado do {@link CourierRegistrationService} seguindo o princípio
 * CQRS (Command Query Responsibility Segregation) na sua forma mais simples:
 * operações de leitura e escrita têm responsabilidades e otimizações distintas.</p>
 *
 * <p>Todas as operações são marcadas com {@code readOnly = true}, o que
 * desativa o dirty checking do Hibernate e melhora a performance de leitura.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourierQueryService {

    private final CourierRepository courierRepository;

    /**
     * Retorna uma página de entregadores cadastrados.
     *
     * @param pageable configuração de paginação e ordenação
     * @return modelo paginado com os entregadores encontrados
     */
    public PagedModel<Courier> findAll(Pageable pageable) {
        return new PagedModel<>(courierRepository.findAll(pageable));
    }

    /**
     * Busca um entregador pelo seu identificador único.
     *
     * @param courierId identificador do entregador
     * @return entregador encontrado
     * @throws DomainException se nenhum entregador for encontrado com o ID informado
     */
    public Courier findById(UUID courierId) {
        return courierRepository.findById(courierId)
                .orElseThrow(() -> new DomainException("Courier not found: " + courierId));
    }
}
