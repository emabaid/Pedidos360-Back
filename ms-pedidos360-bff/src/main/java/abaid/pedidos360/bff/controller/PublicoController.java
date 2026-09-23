package abaid.pedidos360.bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicoController {

    @GetMapping("/publico")
    public Map<String, String> publico() {
        return Map.of("mensaje", "BFF Pedidos360 operativo");
    }
}
