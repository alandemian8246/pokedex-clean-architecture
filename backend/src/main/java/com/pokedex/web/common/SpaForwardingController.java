package com.pokedex.web.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * When the frontend bundle is served by this application (single Docker image), client-side routes such as
 * {@code /pokemon/25} must resolve to {@code index.html}. API, actuator and docs paths, and any path containing a
 * file extension, are excluded.
 */
@Controller
public class SpaForwardingController {

    private static final String ROUTE = "{route:^(?!api$|actuator$|swagger-ui$|v3$|error$)[^.]*}";

    @GetMapping({"/" + ROUTE, "/" + ROUTE + "/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
