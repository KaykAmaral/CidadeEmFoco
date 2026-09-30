package br.com.cidadeemfoco.config;

import br.com.cidadeemfoco.enums.OccurrenceType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
import java.util.Set;

@ConfigurationProperties("app.occurrences.auto-close")
public record AutoClosureProperties(Set<OccurrenceType> types, Duration validity, Duration checkInterval, int batchSize) {
    public AutoClosureProperties {
        types = Set.copyOf(types);
        if (types.stream().anyMatch(type -> !type.isTemporary())) {
            throw new IllegalArgumentException("Encerramento automatico aceita somente tipos temporarios explicitos");
        }
        if (validity == null || validity.compareTo(Duration.ofSeconds(1)) < 0 || validity.getNano() != 0) {
            throw new IllegalArgumentException("A validade deve ser positiva, em segundos inteiros");
        }
        if (checkInterval == null || checkInterval.compareTo(Duration.ofSeconds(1)) < 0) {
            throw new IllegalArgumentException("O intervalo de verificacao deve ser de ao menos um segundo");
        }
        if (batchSize < 1 || batchSize > 1000) throw new IllegalArgumentException("O lote deve ter entre 1 e 1000 registros");
    }
}
