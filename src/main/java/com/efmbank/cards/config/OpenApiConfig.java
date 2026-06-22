package com.efmbank.cards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI cardsOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Cards API")
                .version("1.0.0")
                .description("API системы управления банковскими картами."))
            .components(new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public OpenApiCustomizer pageableExamplesOpenApiCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
            pathItem.readOperations().forEach(operation -> {
                if (operation.getParameters() == null) {
                    return;
                }

                operation.getParameters().forEach(this::applyPageableExample);
            })
        );
    }

    private void applyPageableExample(Parameter parameter) {
        if ("page".equals(parameter.getName())) {
            parameter.setDescription("Номер страницы, начиная с 0.");
            parameter.setExample(0);
            parameter.setSchema(new IntegerSchema()._default(0).minimum(BigDecimal.ZERO));
        }

        if ("size".equals(parameter.getName())) {
            parameter.setDescription("Размер страницы.");
            parameter.setExample(20);
            parameter.setSchema(new IntegerSchema()._default(20).minimum(BigDecimal.ONE));
        }

        if ("sort".equals(parameter.getName())) {
            parameter.setDescription("Сортировка в формате поле,направление. Пример: createdAt,desc");
            parameter.setExample("createdAt,desc");
            parameter.setSchema(new ArraySchema()
                .items(new Schema<String>().type("string").example("createdAt,desc")));
        }
    }
}
