package com.petreca.petrecadelivery.courier.management.api.controller;

import com.petreca.petrecadelivery.courier.management.domain.model.Courier;
import com.petreca.petrecadelivery.courier.management.domain.repository.CourierRepository;
import com.petreca.petrecadelivery.courier.management.infrastructure.PostgreSQLTestContainerConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integração do {@link CourierController}.
 *
 * <p>Sobe o contexto completo do Spring com servidor HTTP em porta aleatória,
 * banco de dados real e todas as camadas integradas (Controller → Service → Repository).
 * Cada teste parte de um banco limpo, garantindo isolamento total.</p>
 *
 * <p>Usa REST Assured para realizar requisições HTTP reais contra o servidor,
 * validando o comportamento completo da API — status codes, headers e body.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgreSQLTestContainerConfig.class)
@DisplayName("CourierController")
class CourierControllerTest {

    /*
     * @LocalServerPort injeta a porta aleatória escolhida pelo Spring.
     * Necessário para configurar o REST Assured apontar para o servidor correto.
     */
    @LocalServerPort
    private int port;

    /*
     * @Autowired em campo é aceitável em testes — não é componente de produção,
     * não há necessidade de constructor injection aqui.
     * O repositório é usado apenas para setup/teardown e criação de fixtures.
     */
    @Autowired
    private CourierRepository courierRepository;

    /*
     * @BeforeEach executa antes de CADA teste.
     * Garante isolamento: cada teste começa com banco limpo
     * e REST Assured configurado corretamente.
     */
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/couriers";

        // Limpa o banco antes de cada teste para garantir isolamento
        // Sem isso, dados de um teste podem afetar o próximo
        courierRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────
    // Helper — cria fixtures reutilizáveis
    // ─────────────────────────────────────────────────────────────────

    /**
     * Cria e persiste um entregador no banco para uso nos testes.
     * Centralizado aqui para evitar repetição e facilitar manutenção.
     */
    private Courier createPersistedCourier(String name, String phone) {
        return courierRepository.saveAndFlush(
                Courier.brandNew(name, phone)
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/couriers
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /")
    class Create {

        @Test
        @DisplayName("should return 201 and persisted courier when input is valid")
        void shouldReturn201WhenInputIsValid() {
            String body = """
                    {
                        "name": "João Silva",
                        "phone": "35998754561"
                    }
                    """;

            given()
                    .body(body)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id",    notNullValue())
                    .body("name",  equalTo("João Silva"))
                    .body("phone", equalTo("35998754561"))
                    .body("pendingDeliveriesQuantity",  equalTo(0))
                    .body("fulfilledDeliveriesQuantity", equalTo(0));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() {
            /*
             * Testa a validação do Bean Validation no controller.
             * @NotBlank em CourierInput.name deve rejeitar strings vazias
             * com HTTP 400 Bad Request.
             */
            String body = """
                    {
                        "name": "",
                        "phone": "35998754561"
                    }
                    """;

            given()
                    .body(body)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("should return 400 when phone is blank")
        void shouldReturn400WhenPhoneIsBlank() {
            String body = """
                    {
                        "name": "João Silva",
                        "phone": ""
                    }
                    """;

            given()
                    .body(body)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // PUT /api/v1/couriers/{courierId}
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /{courierId}")
    class Update {

        @Test
        @DisplayName("should return 200 and updated courier when courier exists")
        void shouldReturn200WhenCourierExists() {
            Courier existing = createPersistedCourier("João Silva", "35998754561");

            String body = """
                    {
                        "name": "João Atualizado",
                        "phone": "35999999999"
                    }
                    """;

            given()
                    .pathParam("courierId", existing.getId())
                    .body(body)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .when()
                    .put("/{courierId}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id",    equalTo(existing.getId().toString()))
                    .body("name",  equalTo("João Atualizado"))
                    .body("phone", equalTo("35999999999"));
        }

        @Test
        @DisplayName("should return 404 when courier does not exist")
        void shouldReturn404WhenCourierNotFound() {
            /*
             * Testa o GlobalExceptionHandler:
             * DomainException lançada pelo service deve ser convertida
             * em HTTP 404 com body no formato Problem Detail (RFC 9457).
             */
            String body = """
                    {
                        "name": "Qualquer Nome",
                        "phone": "35999999999"
                    }
                    """;

            given()
                    .pathParam("courierId", UUID.randomUUID())
                    .body(body)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .when()
                    .put("/{courierId}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("detail", containsString("not found"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/couriers
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /")
    class FindAll {

        @Test
        @DisplayName("should return 200 and paged result with all couriers")
        void shouldReturn200WithPagedCouriers() {
            createPersistedCourier("Courier A", "35991111111");
            createPersistedCourier("Courier B", "35992222222");

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get()
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content",      hasSize(2))
                    .body("page.totalElements", equalTo(2));
        }

        @Test
        @DisplayName("should return 200 and empty content when no couriers exist")
        void shouldReturn200WithEmptyContentWhenNoCouriers() {
            // banco limpo pelo @BeforeEach — nenhum courier existe
            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get()
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content", hasSize(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/couriers/{courierId}
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /{courierId}")
    class FindById {

        @Test
        @DisplayName("should return 200 and courier when courier exists")
        void shouldReturn200WhenCourierExists() {
            Courier existing = createPersistedCourier("Maria Teresa", "35998745531");

            given()
                    .pathParam("courierId", existing.getId())
                    .accept(ContentType.JSON)
                    .when()
                    .get("/{courierId}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id",    equalTo(existing.getId().toString()))
                    .body("name",  equalTo("Maria Teresa"))
                    .body("phone", equalTo("35998745531"));
        }

        @Test
        @DisplayName("should return 404 when courier does not exist")
        void shouldReturn404WhenCourierNotFound() {
            given()
                    .pathParam("courierId", UUID.randomUUID())
                    .accept(ContentType.JSON)
                    .when()
                    .get("/{courierId}")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("detail", containsString("not found"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/couriers/payout-calculation
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /payout-calculation")
    class PayoutCalculation {

        @Test
        @DisplayName("should return 200 and calculated payout for given distance")
        void shouldReturn200WithCalculatedPayout() {
            /*
             * CourierPayoutService calcula: distância × R$10,00
             * Para 5km → R$50,00
             * Verificamos o valor numérico retornado no body.
             */
            String body = """
                    {
                        "distanceInKm": 5.0
                    }
                    """;

            given()
                    .body(body)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .when()
                    .post("/payout-calculation")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("payoutFee", equalTo(50.0f));
        }
    }
}