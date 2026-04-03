package com.petreca.petrecadelivery.courier.management.api.controller;

import com.petreca.petrecadelivery.courier.management.api.doc.CourierControllerDoc;
import com.petreca.petrecadelivery.courier.management.api.exception.GlobalExceptionHandler;
import com.petreca.petrecadelivery.courier.management.api.model.CourierInput;
import com.petreca.petrecadelivery.courier.management.api.model.CourierPayoutCalculationInput;
import com.petreca.petrecadelivery.courier.management.api.model.CourierPayoutResultModel;
import com.petreca.petrecadelivery.courier.management.domain.model.Courier;
import com.petreca.petrecadelivery.courier.management.domain.service.CourierPayoutService;
import com.petreca.petrecadelivery.courier.management.domain.service.CourierQueryService;
import com.petreca.petrecadelivery.courier.management.domain.service.CourierRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operações relacionadas a entregadores.
 *
 * <p>Responsabilidade exclusiva: receber requisições HTTP, delegar
 * para os serviços apropriados e devolver a resposta formatada.
 * Nenhuma regra de negócio deve residir aqui.</p>
 *
 * <p>Tratamento de exceções centralizado em {@link GlobalExceptionHandler}.</p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CourierController implements CourierControllerDoc {

    /*
     * Injeção via construtor — padrão recomendado pelo Spring.
     * @RequiredArgsConstructor do Lombok gera o construtor automaticamente
     * para todos os campos final, eliminando a necessidade de @Autowired.
     *
     * Cada service tem uma responsabilidade bem definida:
     *   CourierRegistrationService → criar e atualizar entregadores
     *   CourierQueryService        → consultar entregadores
     *   CourierPayoutService       → calcular pagamentos
     */
    private final CourierRegistrationService courierRegistrationService;
    private final CourierQueryService courierQueryService;
    private final CourierPayoutService courierPayoutService;

    /**
     * Cria um novo entregador.
     *
     * @param input dados do entregador a ser criado
     * @return entregador criado com status HTTP 201 Created
     */
    @Override
    public ResponseEntity<Void> create(CourierInput input) {
        log.info("Creating new courier via API");
        Courier createdCourier = courierRegistrationService.create(input);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCourier.getId())
                .toUri();
        log.info("Courier registered successfully. Resource Location: {}", location);
        return ResponseEntity.created(location).build();
    }

    /**
     * Atualiza os dados de um entregador existente.
     *
     * @param courierId identificador do entregador
     * @param input     novos dados do entregador
     * @return entregador atualizado com status HTTP 200 OK
     */
    @Override
    public ResponseEntity<Courier> update(UUID courierId, CourierInput input) {
        log.info("Updating courier profile for ID: {}", courierId);
        Courier updatedCourier = courierRegistrationService.update(courierId, input);
        return ResponseEntity.ok(updatedCourier);
    }

    /**
     * Lista entregadores de forma paginada.
     *
     * @param pageable configuração de paginação (page, size, sort)
     * @return página de entregadores
     */
    @Override
    public ResponseEntity<PagedModel<Courier>> findAll(Pageable pageable) {
        log.info("FindAll couriers requested");
        PagedModel<Courier> couriers = courierQueryService.findAll(pageable);
        return ResponseEntity.ok(couriers);
    }

    /**
     * Busca um entregador pelo seu identificador.
     *
     * @param courierId identificador do entregador
     * @return entregador encontrado ou HTTP 404 via GlobalExceptionHandler
     */
    @Override
    public ResponseEntity<Courier> findById(UUID courierId) {
        log.info("Fetching courier by ID: {}", courierId);
        Courier courier =courierQueryService.findById(courierId);
        return ResponseEntity.ok(courier);
    }

    /**
     * Calcula o pagamento de um entregador com base na distância percorrida.
     *
     * @param input distância em km
     * @return valor calculado do pagamento
     */
    @Override
    public ResponseEntity<CourierPayoutResultModel> calculate(CourierPayoutCalculationInput input) {
        log.info("Payout calculation requested for distance: {} km", input.distanceInKm());
        BigDecimal payoutFee = courierPayoutService.calculate(input.distanceInKm());
        CourierPayoutResultModel resultModel = new CourierPayoutResultModel(payoutFee);
        return ResponseEntity.ok(resultModel);
    }
}