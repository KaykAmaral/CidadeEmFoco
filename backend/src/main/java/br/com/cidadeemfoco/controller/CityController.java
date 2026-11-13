package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    @Operation(summary = "Listar cidades disponiveis para cadastro", security = {})
    public List<CityResponse> findActive() {
        return cityService.findActive();
    }

    @GetMapping(value = "/{ibgeCode}/boundary", produces = "application/geo+json")
    @Operation(summary = "Consultar o limite geografico de uma cidade ativa")
    public String findBoundary(
            @PathVariable
            @Pattern(regexp = "\\d{7}", message = "Informe um codigo IBGE valido")
            String ibgeCode
    ) {
        return cityService.findBoundary(ibgeCode);
    }
}
