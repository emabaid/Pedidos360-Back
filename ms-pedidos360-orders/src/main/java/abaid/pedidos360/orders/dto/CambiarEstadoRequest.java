package abaid.pedidos360.orders.dto;

import abaid.pedidos360.orders.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public class CambiarEstadoRequest {

    @NotNull
    private EstadoPedido nuevoEstado;

    public EstadoPedido getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(EstadoPedido nuevoEstado) { this.nuevoEstado = nuevoEstado; }
}
