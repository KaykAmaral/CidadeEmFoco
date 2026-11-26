package br.com.cidadeemfoco.repository;

public interface CityRankingMetricsProjection {

    Long getCityId();

    Long getTotalCases();

    Long getResolvedCases();

    Long getNotConfirmedCases();
}
