package com.petreca.petrecadelivery.delivery.tracking.infrastructure;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuração de Testcontainers para testes de integração.
 *
 * @ServiceConnection detecta automaticamente o tipo do container
 * e sobrescreve as propriedades de datasource do application.yml de teste,
 * apontando para o container que foi criado dinamicamente.
 * Não é necessário hardcodar porta ou URL — o Spring gerencia tudo.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgreSQLTestContainerConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                .withDatabaseName("deliverydb_test")
                .withUsername("test")
                .withPassword("test");
    }
}
