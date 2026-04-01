package com.duoc.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class GatewayController {

    @Autowired
    private RestTemplate rest;

    // 🔥 Compra → Order Service
    @PostMapping("/comprar")
    public String comprar(@RequestBody Object order) {
        return rest.postForObject(
                "http://localhost:8082/orders",
                order,
                String.class);
    }

    // 📦 Productos → Product Service
    @GetMapping("/productos")
    public Object getProductos() {
        return rest.getForObject(
                "http://localhost:8081/productos",
                Object.class);
    }

    // 👤 Login → User Service
    @PostMapping("/login")
    public Object login(@RequestBody Object creds) {
        return rest.postForObject(
                "http://localhost:8084/users/login",
                creds,
                Object.class);
    }
}