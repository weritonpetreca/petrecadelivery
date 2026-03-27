package com.petreca.petrecadelivery.courier.management.domain.service;

import com.petreca.petrecadelivery.courier.management.api.model.CourierInput;
import com.petreca.petrecadelivery.courier.management.domain.exception.DomainException;
import com.petreca.petrecadelivery.courier.management.domain.model.Courier;
import com.petreca.petrecadelivery.courier.management.domain.repository.CourierRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pelas operações de escrita do ciclo de vida
 * de entregadores: criação e atualização de dados cadastrais.
 *
 * <p>Separado do {@link CourierQueryService} seguindo o princípio CQRS
 * na sua forma mais simples — operações de comando (escrita) e consulta
 * (leitura) têm responsabilidades, otimizações e ciclos de vida distintos.</p>
 *
 * <p>Todas as operações são transacionais ({@code @Transactional}), garantindo
 * atomicidade — em caso de falha, o banco retorna ao estado anterior
 * automaticamente via rollback.</p>
 *
 * <p>A validação dos dados de entrada ({@link CourierInput}) é responsabilidade
 * da camada de apresentação via {@code @Valid} nos controllers. Não é necessário
 * revalidar aqui, pois o service só é invocado após a validação ter ocorrido.</p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CourierRegistrationService {

    private final CourierRepository courierRepository;

    /**
     * Cadastra um novo entregador na plataforma.
     *
     * <p>Usa o factory method {@link Courier#brandNew(String, String)}
     * para garantir que a entidade nasce em estado válido e consistente,
     * com ID gerado e contadores inicializados.</p>
     *
     * @param input dados do novo entregador (nome e telefone)
     * @return entregador persistido com ID gerado
     */
    public Courier create(CourierInput input) {
        log.info("Creating new courier with name: {}", input.getName());

        Courier courier = Courier.brandNew(input.getName(), input.getPhone());
        Courier saved = courierRepository.saveAndFlush(courier);

        log.info("Courier created successfully with id: {}", saved.getId());
        return saved;
    }

    /**
     * Atualiza os dados cadastrais de um entregador existente.
     *
     * <p>Apenas nome e telefone são atualizáveis — dados operacionais
     * como contadores de entrega e histórico são gerenciados internamente
     * pelo domínio e não podem ser alterados diretamente.</p>
     *
     * @param courierId identificador do entregador a ser atualizado
     * @param input     novos dados cadastrais
     * @return entregador atualizado
     * @throws DomainException se nenhum entregador for encontrado com o ID informado
     */
    public Courier update(UUID courierId, CourierInput input) {
        log.info("Updating courier with id: {}", courierId);

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new DomainException(
                        "Courier not found: " + courierId));

        courier.setPhone(input.getPhone());
        courier.setName(input.getName());
        Courier updated = courierRepository.saveAndFlush(courier);

        log.info("Courier updated successfully with id: {}", courierId);
        return updated;
    }
}
