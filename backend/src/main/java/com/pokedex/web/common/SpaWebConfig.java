package com.pokedex.web.common;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Serves the bundled single-page app (single Docker image). Existing static files are returned as-is; client-side
 * routes such as {@code /pokemon/25} fall back to {@code index.html}. API paths and missing files (anything with an
 * extension) still produce a real 404.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    private static final String INDEX = "index.html";

    private final String[] staticLocations;

    public SpaWebConfig(WebProperties webProperties) {
        this.staticLocations = webProperties.getResources().getStaticLocations();
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(staticLocations)
                .resourceChain(true)
                .addResolver(new SpaFallbackResolver());
    }

    static final class SpaFallbackResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
            Resource requested = location.createRelative(resourcePath);
            if (requested.exists() && requested.isReadable()) {
                return requested;
            }
            if (resourcePath.startsWith("api/") || resourcePath.contains(".")) {
                return null;
            }
            Resource index = location.createRelative(INDEX);
            return index.exists() && index.isReadable() ? index : null;
        }
    }
}
