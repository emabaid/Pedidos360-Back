package abaid.pedidos360.orders.repository;

import abaid.pedidos360.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClienteUsername(String clienteUsername);
}
