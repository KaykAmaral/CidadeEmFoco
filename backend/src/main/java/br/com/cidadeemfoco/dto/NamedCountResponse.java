package br.com.cidadeemfoco.dto;

public record NamedCountResponse(
        String name,
        long count
) {
}
