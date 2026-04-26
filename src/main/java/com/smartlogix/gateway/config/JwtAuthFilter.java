package com.smartlogix.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Filtro JWT centralizado en el API Gateway.
 * Valida el token Bearer en TODAS las rutas protegidas antes de
 * hacer forwarding al microservicio destino.
 *
 * Rutas públicas (sin filtro): /api/auth/**  (login/registro en ms-usuarios)
 * Rutas protegidas: /api/inventario/**, /api/pedidos/**,
 *                   /api/envios/**, /api/notificaciones/**
 */
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            // Verificar presencia del header Authorization con Bearer
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            // Validar el token JWT
            if (!jwtUtil.isTokenValid(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Propagar el username como header interno al microservicio destino
            String username = jwtUtil.extractUsername(token);
            var mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Auth-User", username)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    public static class Config {
        // Configuración adicional si se requiere en el futuro
    }
}