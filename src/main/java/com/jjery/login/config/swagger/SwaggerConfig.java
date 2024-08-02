package com.jjery.login.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Bean
  public OpenAPI customOpenAPI() {
    Server localServer = new Server();
    localServer.setUrl(contextPath);
    localServer.setDescription("Local Server");

    return new OpenAPI()
        .addServersItem(localServer)
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .info(
            new Info()
                .title("login 모듈 API")
                .version("1.0")
                .description(
                    "API Test용 ID : jjery Token : eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJqamVyeSIsImlhdCI6MTcyMjYxMTA3OCwiZXhwIjoxNzIzMjE1ODc4fQ.-Dp7nzqsqhRqbH-8tG2eyFH-tvARgbU8MOlMrHfCd2o"));
  }

  @Bean
  public GroupedOpenApi customGroupedOpenApi() {
    return GroupedOpenApi.builder().group("api").pathsToMatch("/**").build();
  }
}
