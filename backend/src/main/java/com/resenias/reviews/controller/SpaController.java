package com.resenias.reviews.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Reenvía solo las rutas del frontend SPA a index.html para no interceptar assets.
 */
@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/explore",
        "/login",
        "/oauth2/callback",
        "/verify-phone",
        "/profile",
        "/profile/reviews",
        "/owner/dashboard",
        "/admin",
        "/business/{id}"
    })
    public String spa() {
        return "forward:/index.html";
    }
}
