package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OccurrenceTest {

    private final User citizen = new User(
            "Cidadao Teste",
            "cidadao@example.com",
            "$2a$10$hashSomenteParaTeste",
            UserRole.CITIZEN
    );

    @Test
    void shouldStartAsRegistered() {
        Occurrence occurrence = new Occurrence(
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                "Alagamento observado na via",
                PerceivedRisk.ALTO,
                new BigDecimal("-24.008100"),
                new BigDecimal("-46.412000"),
                "Boqueirao",
                null,
                citizen
        );

        assertThat(occurrence.getStatus()).isEqualTo(OccurrenceStatus.REGISTRADA);
    }

    @Test
    void shouldRejectTypeFromAnotherCategory() {
        assertThatThrownBy(() -> new Occurrence(
                OccurrenceCategory.INFRAESTRUTURA_URBANA,
                OccurrenceType.ENCHENTE,
                "Combinacao invalida",
                PerceivedRisk.MEDIO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                citizen
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria");
    }
}

