package abaid.pedidos360.catalog.controller;

import abaid.pedidos360.catalog.dto.StockUpdateRequest;
import abaid.pedidos360.catalog.model.Product;
import abaid.pedidos360.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Microservicio PRIVADO / INTERNO.
 * Solo debe ser accesible desde ms-pedidos360-bff (regla de red /
 * seguridad de despliegue). No vuelve a validar JWT: la autenticacion
 * y autorizacion por rol ya se resolvieron en el API Gateway y en el BFF.
 */
@RestController
@RequestMapping("/api/catalog")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Product obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product crear(@Valid @RequestBody Product producto) {
        return service.crear(producto);
    }

    @PutMapping("/{id}")
    public Product actualizar(@PathVariable Long id, @Valid @RequestBody Product producto) {
        return service.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @PatchMapping("/{id}/stock/disminuir")
    public Product disminuirStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest req) {
        return service.disminuirStock(id, req.getCantidad());
    }

    @PatchMapping("/{id}/stock/aumentar")
    public Product aumentarStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest req) {
        return service.aumentarStock(id, req.getCantidad());
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<String> notFound(java.util.NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> conflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
