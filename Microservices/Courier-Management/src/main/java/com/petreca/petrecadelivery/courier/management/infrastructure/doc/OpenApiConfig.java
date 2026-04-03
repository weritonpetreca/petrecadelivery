package com.petreca.petrecadelivery.courier.management.infrastructure.doc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Courier Management API", version = "v1", contact = @Contact(name = "Weriton L. Petreca", email = "eulcfr@gmail.com", url = "https://weriton.dev")),
        servers = @Server(url = "http://localhost:9999", description = "API Gateway"),
        security = @SecurityRequirement(name = "Keycloak-JWT")
)
@SecurityScheme(
        name = "Keycloak-JWT",
        type = SecuritySchemeType.OAUTH2,
        description = "Oauth2 flow using Keycloak",
        flows = @OAuthFlows(
                password = @OAuthFlow(
                        tokenUrl = "http://localhost:8082/realms/petreca-realm/protocol/openid-connect/token"
                )
        )
)
public class OpenApiConfig {
}
