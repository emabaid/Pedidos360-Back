# Pedidos360 - Backend

|Servicio|Puerto|Rol|
|-|-|-|
|ms-pedidos360-catalog|8082|CRUD de productos, precios y stock. Privado, solo lo llama el BFF.|
|ms-pedidos360-orders|8081|CRUD de pedidos y máquina de estados. Privado, solo lo llama el BFF. Le pide productos y descuenta stock a catalog.|
|ms-pedidos360-bff|8080|Único punto expuesto al frontend. Valida el JWT de Entra ID (issuer, audience, firma, vigencia) y aplica autorización por rol antes de reenviar a orders/catalog.|

1. Necesitas Java 17+ y Maven.
2. En terminales separadas:

```
   cd ms-pedidos360-catalog \&\& mvn spring-boot:run
   cd ms-pedidos360-orders  \&\& mvn spring-boot:run
   cd ms-pedidos360-bff     \&\& mvn spring-boot:run
```

