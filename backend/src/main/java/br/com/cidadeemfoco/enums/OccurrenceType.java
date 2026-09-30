package br.com.cidadeemfoco.enums;

public enum OccurrenceType {
    ALAGAMENTO(true),
    ENCHENTE(true),
    QUEDA_ARVORE,
    DESLIZAMENTO,
    VENTOS_FORTES(true),
    RESSACA_MARITIMA(true),
    INCENDIO,
    CHUVA_INTENSA(true),
    QUEDA_GRANIZO(true),
    BURACO_RUA,
    BUEIRO_ENTUPIDO,
    POSTE_DANIFICADO,
    SEMAFORO_COM_PROBLEMA,
    RUA_BLOQUEADA,
    FALTA_ILUMINACAO,
    ARVORE_OBSTRUINDO_VIA,
    OUTRO;

    private final boolean temporary;

    OccurrenceType() {
        this(false);
    }

    OccurrenceType(boolean temporary) {
        this.temporary = temporary;
    }

    public boolean isTemporary() {
        return temporary;
    }
}

