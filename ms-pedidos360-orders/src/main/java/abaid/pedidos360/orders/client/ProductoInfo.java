package abaid.pedidos360.orders.client;

/** Representacion minima del producto tal como lo entrega ms-pedidos360-catalog. */
public class ProductoInfo {
    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
