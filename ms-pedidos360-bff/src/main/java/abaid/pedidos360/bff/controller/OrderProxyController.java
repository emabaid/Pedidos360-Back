package abaid.pedidos360.bff.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Proxy hacia ms-pedidos360-orders.
 *
 * PRINCIPIO CLAVE (ajustado tras la retroalimentación del profesor): la
 * seguridad NUNCA depende de que el cliente llame al endpoint "correcto".
 * Antes existían dos rutas separadas (/api/orders para todos,
 * /api/orders/mios para el propio Cliente); si por error de configuración
 * esa segunda ruta fallaba o alguien probaba la primera, un Cliente podría
 * haber visto pedidos ajenos. Ahora hay UNA sola ruta GET /api/orders que
 * decide sola, mirando los roles ya validados del JWT, si te trae todos los
 * pedidos o solo los tuyos. Lo mismo se aplica a GET /api/orders/{id}: se
 * verifica que el pedido pedido realmente le pertenezca al Cliente antes de
 * devolverlo, no importa qué ID haya puesto en la URL.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderProxyController {

    private final RestClient ordersRestClient;

    public OrderProxyController(RestClient ordersRestClient) {
        this.ordersRestClient = ordersRestClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public List<Map<String, Object>> listar(Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        if (esGestor(authentication)) {
            // Administrador u Operador: todos los pedidos.
            return ordersRestClient.get()
                    .uri("/api/orders")
                    .retrieve()
                    .body(List.class);
        }

        // Cliente: el filtro sale del JWT, jamás de un parámetro que mande el navegador.
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
    public Map<String, Object> obtener(@PathVariable Long id, Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> pedido = ordersRestClient.get()
                .uri("/api/orders/{id}", id)
                .retrieve()
                .body(Map.class);

        if (!esGestor(authentication)) {
            String usuario = usuarioDesde(jwt);
            Object dueño = pedido != null ? pedido.get("clienteUsername") : null;

            if (!usuario.equals(dueño)) {
                // No se revela si el pedido existe o no, solo que no es tuyo.
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este pedido no te pertenece");
            }
        }

        return pedido;
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

    /** Administrador u Operador pueden ver/gestionar pedidos de cualquier cliente. */
    private boolean esGestor(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String rol = authority.getAuthority();
            if (rol.equals("ROLE_ADMINISTRADOR") || rol.equals("ROLE_OPERADOR")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Identifica al usuario dueño del pedido usando SOLO datos del JWT ya
     * validado (issuer, audience y firma verificados por SecurityConfig),
     * nunca confiando en nada que mande el navegador. Se prueban 3 claims
     * en orden porque el access token de una API custom en Entra ID no
     * siempre trae "email" ni "preferred_username" salvo que se hayan
     * agregado como "optional claims" en la App Registration de la API
     * (Token configuration -> Add optional claim -> Access token -> email).
     * "sub" (subject) siempre viene, así que es el último fallback seguro.
     */
    private String usuarioDesde(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null) return email;

        String upn = jwt.getClaimAsString("preferred_username");
        if (upn != null) return upn;

        return jwt.getSubject();
    }
}
