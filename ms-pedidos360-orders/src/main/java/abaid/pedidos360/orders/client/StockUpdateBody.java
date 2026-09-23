package abaid.pedidos360.orders.client;

public class StockUpdateBody {
    private Integer cantidad;
    public StockUpdateBody() {}
    public StockUpdateBody(Integer cantidad) { this.cantidad = cantidad; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
