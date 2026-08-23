package br.com.cidadeemfoco.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static br.com.cidadeemfoco.enums.OccurrenceType.ALAGAMENTO;
import static br.com.cidadeemfoco.enums.OccurrenceType.ARVORE_OBSTRUINDO_VIA;
import static br.com.cidadeemfoco.enums.OccurrenceType.BUEIRO_ENTUPIDO;
import static br.com.cidadeemfoco.enums.OccurrenceType.BURACO_RUA;
import static br.com.cidadeemfoco.enums.OccurrenceType.DESLIZAMENTO;
import static br.com.cidadeemfoco.enums.OccurrenceType.CHUVA_INTENSA;
import static br.com.cidadeemfoco.enums.OccurrenceType.ENCHENTE;
import static br.com.cidadeemfoco.enums.OccurrenceType.FALTA_ILUMINACAO;
import static br.com.cidadeemfoco.enums.OccurrenceType.INCENDIO;
import static br.com.cidadeemfoco.enums.OccurrenceType.OUTRO;
import static br.com.cidadeemfoco.enums.OccurrenceType.POSTE_DANIFICADO;
import static br.com.cidadeemfoco.enums.OccurrenceType.QUEDA_ARVORE;
import static br.com.cidadeemfoco.enums.OccurrenceType.RESSACA_MARITIMA;
import static br.com.cidadeemfoco.enums.OccurrenceType.RUA_BLOQUEADA;
import static br.com.cidadeemfoco.enums.OccurrenceType.SEMAFORO_COM_PROBLEMA;
import static br.com.cidadeemfoco.enums.OccurrenceType.VENTOS_FORTES;

public enum OccurrenceCategory {
    EVENTO_NATURAL(
            ALAGAMENTO,
            ENCHENTE,
            QUEDA_ARVORE,
            DESLIZAMENTO,
            VENTOS_FORTES,
            RESSACA_MARITIMA,
            INCENDIO,
            CHUVA_INTENSA,
            OUTRO
    ),
    INFRAESTRUTURA_URBANA(
            BURACO_RUA,
            BUEIRO_ENTUPIDO,
            POSTE_DANIFICADO,
            SEMAFORO_COM_PROBLEMA,
            RUA_BLOQUEADA,
            FALTA_ILUMINACAO,
            ARVORE_OBSTRUINDO_VIA,
            OUTRO
    );

    private final Set<OccurrenceType> allowedTypes;

    OccurrenceCategory(OccurrenceType firstType, OccurrenceType... otherTypes) {
        this.allowedTypes = Collections.unmodifiableSet(EnumSet.of(firstType, otherTypes));
    }

    public boolean allows(OccurrenceType type) {
        return type != null && allowedTypes.contains(type);
    }

    public Set<OccurrenceType> getAllowedTypes() {
        return allowedTypes;
    }
}

