package abaid.pedidos360.catalog.dto;

import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {

    @NotNull
    private Integer cantidad;

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
