package abaid.pedidos360.orders.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente interno hacia ms-pedidos360-catalog.
 * ms-pedidos360-orders es el unico consumidor de este cliente: la llamada
 * nunca sale directamente desde el navegador, siempre pasa antes por el BFF.
 */
@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(@Value("${pedidos360.catalog-base-url}") String catalogBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(catalogBaseUrl).build();
    }

    public ProductoInfo obtenerProducto(Long productoId) {
        return restClient.get()
                .uri("/api/catalog/{id}", productoId)
                .retrieve()
                .body(ProductoInfo.class);
    }

    public void disminuirStock(Long productoId, int cantidad) {
        restClient.patch()
                .uri("/api/catalog/{id}/stock/disminuir", productoId)
                .body(new StockUpdateBody(cantidad))
                .retrieve()
                .toBodilessEntity();
    }

    public void aumentarStock(Long productoId, int cantidad) {
        restClient.patch()
                .uri("/api/catalog/{id}/stock/aumentar", productoId)
                .body(new StockUpdateBody(cantidad))
                .retrieve()
                .toBodilessEntity();
    }
}
