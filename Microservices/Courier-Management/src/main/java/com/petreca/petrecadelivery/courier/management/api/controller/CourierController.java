package com.petreca.petrecadelivery.courier.management.api.controller;

import com.petreca.petrecadelivery.courier.management.api.exception.GlobalExceptionHandler;
import com.petreca.petrecadelivery.courier.management.api.model.CourierInput;
import com.petreca.petrecadelivery.courier.management.api.model.CourierPayoutCalculationInput;
import com.petreca.petrecadelivery.courier.management.api.model.CourierPayoutResultModel;
import com.petreca.petrecadelivery.courier.management.domain.model.Courier;
import com.petreca.petrecadelivery.courier.management.domain.service.CourierPayoutService;
import com.petreca.petrecadelivery.courier.management.domain.service.CourierQueryService;
import com.petreca.petrecadelivery.courier.management.domain.service.CourierRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
@Slf4j
public class CourierController {

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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Courier create(@Valid @RequestBody CourierInput input) {
        return courierRegistrationService.create(input);
    }

    /**
     * Atualiza os dados de um entregador existente.
     *
     * @param courierId identificador do entregador
     * @param input     novos dados do entregador
     * @return entregador atualizado com status HTTP 200 OK
     */
    @PutMapping("/{courierId}")
    public Courier update(@PathVariable UUID courierId,
                          @Valid @RequestBody CourierInput input) {
        return courierRegistrationService.update(courierId, input);
    }

    /**
     * Lista entregadores de forma paginada.
     *
     * @param pageable configuração de paginação (page, size, sort)
     * @return página de entregadores
     */
    @GetMapping
    public PagedModel<Courier> findAll(@PageableDefault Pageable pageable) {
        log.info("FindAll couriers requested");
        return courierQueryService.findAll(pageable);
    }

    /**
     * Busca um entregador pelo seu identificador.
     *
     * @param courierId identificador do entregador
     * @return entregador encontrado ou HTTP 404 via GlobalExceptionHandler
     */
    @GetMapping("/{courierId}")
    public Courier findById(@PathVariable UUID courierId) {
        return courierQueryService.findById(courierId);
    }

    /**
     * Calcula o pagamento de um entregador com base na distância percorrida.
     *
     * @param input distância em km
     * @return valor calculado do pagamento
     */
    @PostMapping("/payout-calculation")
    public CourierPayoutResultModel calculate(
            @RequestBody CourierPayoutCalculationInput input) {
        log.info("Payout calculation requested for distance: {} km",
                input.distanceInKm());
        BigDecimal payoutFee = courierPayoutService.calculate(input.distanceInKm());
        return new CourierPayoutResultModel(payoutFee);
    }
}