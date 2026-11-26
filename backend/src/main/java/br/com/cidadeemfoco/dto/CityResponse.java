package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.City;

public record CityResponse(
        Long id,
        String name,
        String stateCode,
        String ibgeCode
) {
    public static CityResponse from(City city) {
        return new CityResponse(city.getId(), city.getName(), city.getStateCode(), city.getIbgeCode());
    }
}
