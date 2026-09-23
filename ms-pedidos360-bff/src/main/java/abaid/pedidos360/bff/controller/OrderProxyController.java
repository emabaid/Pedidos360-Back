package abaid.pedidos360.bff.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Proxy hacia ms-pedidos360-orders. Reglas de autorizacion segun la tabla de
 * diferenciacion de actores (seccion 9 del documento de la evaluacion):
 *  - Ver todos los pedidos: Administrador y Operador.
 *  - Ver "mis pedidos": Cliente (el BFF fuerza el username desde el propio
 *    JWT, nunca confia en lo que mande el frontend).
 *  - Crear pedido: los tres roles.
 *  - Cambiar estado: solo Operador.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderProxyController {

    private final RestClient ordersRestClient;

    public OrderProxyController(RestClient ordersRestClient) {
        this.ordersRestClient = ordersRestClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR')")
    public List<Map<String, Object>> listarTodos() {
        return ordersRestClient.get()
                .uri("/api/orders")
                .retrieve()
                .body(List.class);
    }

    @GetMapping("/mios")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<Map<String, Object>> listarMios(@AuthenticationPrincipal Jwt jwt) {
        String usuario = usuarioDesde(jwt);

        return ordersRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/orders/mios")
                        .queryParam("usuario", usuario)
                        .build())
                .retrieve()
                .body(List.class);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public Map<String, Object> obtener(@PathVariable Long id) {
        return ordersRestClient.get()
                .uri("/api/orders/{id}", id)
                .retrieve()
                .body(Map.class);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public Map<String, Object> crear(@RequestBody Map<String, Object> pedido, @AuthenticationPrincipal Jwt jwt) {
        // El cliente autenticado siempre crea el pedido a su propio nombre,
        // sin importar lo que venga en el body.
        pedido.put("clienteUsername", usuarioDesde(jwt));

        return ordersRestClient.post()
                .uri("/api/orders")
                .body(pedido)
                .retrieve()
                .body(Map.class);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('OPERADOR')")
    public Map<String, Object> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ordersRestClient.patch()
                .uri("/api/orders/{id}/estado", id)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private String usuarioDesde(Jwt jwt) {
        String usuario = jwt.getClaimAsString("preferred_username");
        return usuario != null ? usuario : jwt.getSubject();
    }
}
