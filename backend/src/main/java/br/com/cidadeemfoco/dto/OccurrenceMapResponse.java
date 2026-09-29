package br.com.cidadeemfoco.dto;

import java.util.List;

public record OccurrenceMapResponse(List<OccurrenceResponse> occurrences, long refreshAfterMillis) {
}
