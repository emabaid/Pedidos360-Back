package abaid.pedidos360.catalog.repository;

import abaid.pedidos360.catalog.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
