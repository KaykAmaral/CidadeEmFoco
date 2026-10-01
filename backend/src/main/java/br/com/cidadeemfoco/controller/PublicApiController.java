package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.ClimateAlertResponse;
import br.com.cidadeemfoco.dto.PageResponse;
import br.com.cidadeemfoco.dto.PublicOccurrenceResponse;
import br.com.cidadeemfoco.service.ClimateAlertService;
import br.com.cidadeemfoco.service.OccurrenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "API pública v1", description = "Dados públicos sem informações pessoais de cidadãos")
@SecurityRequirements
public class PublicApiController {

    private final ClimateAlertService climateAlertService;
    private final OccurrenceService occurrenceService;

    public PublicApiController(ClimateAlertService climateAlertService, OccurrenceService occurrenceService) {
        this.climateAlertService = climateAlertService;
        this.occurrenceService = occurrenceService;
    }

    @GetMapping("/alerts/active")
    @Operation(summary = "Listar alertas climáticos ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alertas ativos retornados"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno padronizado")
    })
    public List<ClimateAlertResponse> findActiveAlerts() {
        return climateAlertService.findCurrentlyActive();
    }

    @GetMapping("/occurrences")
    @Operation(summary = "Listar resumos públicos de ocorrências", description = "Não retorna texto livre, endereço, coordenadas, imagens ou identidade do cidadão.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de ocorrências retornada"),
            @ApiResponse(responseCode = "400", description = "Paginação inválida"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno padronizado")
    })
    public PageResponse<PublicOccurrenceResponse> findOccurrences(
            @Parameter(description = "Página iniciada em zero", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Itens por página, máximo 100", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return occurrenceService.findPublic(PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdAt")
        ));
    }
}
