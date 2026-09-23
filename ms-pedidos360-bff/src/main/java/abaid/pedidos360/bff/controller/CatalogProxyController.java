package abaid.pedidos360.bff.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Proxy hacia ms-pedidos360-catalog. El BFF ya valido el JWT (issuer,
 * audience, firma, vigencia) en SecurityConfig; aqui solo se aplica la
 * autorizacion por rol antes de delegar la llamada al microservicio.
 *
 * NOTA IMPORTANTE (revisar con el ramo): el documento de la evaluacion trae
 * una contradiccion entre la seccion 15 (dice que el Administrador
 * "administra productos, stock y la operacion global") y la tabla de la
 * seccion 9 (marca "No" para el Administrador en Crear/editar producto y
 * Administrar stock, dejando esa responsabilidad solo al Operador). Aqui se
 * implemento la tabla de la seccion 9 (Operador administra catalogo). Si tu
 * profesor confirma que el Administrador tambien debe poder hacerlo, basta
 * con agregar "ADMINISTRADOR" a los hasAnyRole(...) de abajo.
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogProxyController {

    private final RestClient catalogRestClient;

    public CatalogProxyController(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public List<Map<String, Object>> listar() {
        return catalogRestClient.get()
                .uri("/api/catalog")
                .retrieve()
                .body(List.class);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','OPERADOR','CLIENTE')")
    public Map<String, Object> obtener(@PathVariable Long id) {
        return catalogRestClient.get()
                .uri("/api/catalog/{id}", id)
                .retrieve()
                .body(Map.class);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR')")
    public Map<String, Object> crear(@RequestBody Map<String, Object> producto) {
        return catalogRestClient.post()
                .uri("/api/catalog")
                .body(producto)
                .retrieve()
                .body(Map.class);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public Map<String, Object> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> producto) {
        return catalogRestClient.put()
                .uri("/api/catalog/{id}", id)
                .body(producto)
                .retrieve()
                .body(Map.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OPERADOR')")
    public void eliminar(@PathVariable Long id) {
        catalogRestClient.delete()
                .uri("/api/catalog/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
