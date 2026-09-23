package abaid.pedidos360.orders.service;

import abaid.pedidos360.orders.client.CatalogClient;
import abaid.pedidos360.orders.client.ProductoInfo;
import abaid.pedidos360.orders.dto.CrearPedidoRequest;
import abaid.pedidos360.orders.dto.ItemPedidoRequest;
import abaid.pedidos360.orders.model.EstadoPedido;
import abaid.pedidos360.orders.model.Order;
import abaid.pedidos360.orders.model.OrderItem;
import abaid.pedidos360.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final CatalogClient catalogClient;

    /** Transiciones permitidas: CREADO -> ACEPTADO -> EN_PREPARACION -> DESPACHADO -> ENTREGADO / CANCELADO. */
    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = new EnumMap<>(EstadoPedido.class);
    static {
        TRANSICIONES.put(EstadoPedido.CREADO, EnumSet.of(EstadoPedido.ACEPTADO, EstadoPedido.CANCELADO));
        TRANSICIONES.put(EstadoPedido.ACEPTADO, EnumSet.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO));
        TRANSICIONES.put(EstadoPedido.EN_PREPARACION, EnumSet.of(EstadoPedido.DESPACHADO, EstadoPedido.CANCELADO));
        TRANSICIONES.put(EstadoPedido.DESPACHADO, EnumSet.of(EstadoPedido.ENTREGADO));
        TRANSICIONES.put(EstadoPedido.ENTREGADO, EnumSet.noneOf(EstadoPedido.class));
        TRANSICIONES.put(EstadoPedido.CANCELADO, EnumSet.noneOf(EstadoPedido.class));
    }

    public OrderService(OrderRepository repository, CatalogClient catalogClient) {
        this.repository = repository;
        this.catalogClient = catalogClient;
    }

    public List<Order> listarTodos() {
        return repository.findAll();
    }

    public List<Order> listarPorCliente(String clienteUsername) {
        return repository.findByClienteUsername(clienteUsername);
    }

    public Order obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + id));
    }

    public Order crear(CrearPedidoRequest request) {
        Order order = new Order();
        order.setClienteUsername(request.getClienteUsername());
        order.setEstado(EstadoPedido.CREADO);

        for (ItemPedidoRequest itemReq : request.getItems()) {
            ProductoInfo producto = catalogClient.obtenerProducto(itemReq.getProductoId());

            OrderItem item = new OrderItem();
            item.setProductoId(producto.getId());
            item.setProductoNombre(producto.getNombre());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(producto.getPrecio());
            item.setOrder(order);
            order.getItems().add(item);
        }

        return repository.save(order);
    }

    /**
     * Cambia el estado de un pedido respetando la maquina de estados.
     * Regla clave del negocio: no se puede DESPACHAR sin ACEPTAR antes.
     * Al ACEPTAR el pedido, se descuenta el stock en ms-pedidos360-catalog.
     */
    public Order cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Order order = obtener(id);
        EstadoPedido actual = order.getEstado();

        Set<EstadoPedido> permitidos = TRANSICIONES.getOrDefault(actual, EnumSet.noneOf(EstadoPedido.class));
        if (!permitidos.contains(nuevoEstado)) {
            throw new IllegalStateException(
                "Transicion invalida: no se puede pasar de " + actual + " a " + nuevoEstado
                + " (ej.: no se puede DESPACHAR sin ACEPTAR)"
            );
        }

        if (nuevoEstado == EstadoPedido.ACEPTADO) {
            // Descontar stock de cada item al aceptar el pedido.
            for (OrderItem item : order.getItems()) {
                catalogClient.disminuirStock(item.getProductoId(), item.getCantidad());
            }
        }

        order.setEstado(nuevoEstado);
        return repository.save(order);
    }
}
