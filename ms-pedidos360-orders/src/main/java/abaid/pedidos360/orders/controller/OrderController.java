package abaid.pedidos360.orders.controller;

import abaid.pedidos360.orders.dto.CambiarEstadoRequest;
import abaid.pedidos360.orders.dto.CrearPedidoRequest;
import abaid.pedidos360.orders.model.Order;
import abaid.pedidos360.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Microservicio PRIVADO / INTERNO. Solo accesible desde ms-pedidos360-bff.
 * La autorizacion por rol (quien puede ver todos los pedidos, quien puede
 * cambiar estados, etc.) se resuelve en el BFF antes de llegar aqui; este
 * servicio confia en los parametros/headers que el BFF ya valido.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<Order> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/mios")
    public List<Order> listarMios(@RequestParam String usuario) {
        return service.listarPorCliente(usuario);
    }

    @GetMapping("/{id}")
    public Order obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order crear(@Valid @RequestBody CrearPedidoRequest request) {
        return service.crear(request);
    }

    @PatchMapping("/{id}/estado")
    public Order cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return service.cambiarEstado(id, request.getNuevoEstado());
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
