ALTER TABLE occurrences
    DROP CHECK chk_occurrences_category_type;

ALTER TABLE occurrences
    ADD CONSTRAINT chk_occurrences_category_type CHECK (
        (category = 'EVENTO_NATURAL' AND occurrence_type IN (
            'ALAGAMENTO', 'ENCHENTE', 'QUEDA_ARVORE', 'DESLIZAMENTO',
            'VENTOS_FORTES', 'RESSACA_MARITIMA', 'INCENDIO',
            'CHUVA_INTENSA', 'OUTRO'
        ))
        OR
        (category = 'INFRAESTRUTURA_URBANA' AND occurrence_type IN (
            'BURACO_RUA', 'BUEIRO_ENTUPIDO', 'POSTE_DANIFICADO',
            'SEMAFORO_COM_PROBLEMA', 'RUA_BLOQUEADA', 'FALTA_ILUMINACAO',
            'ARVORE_OBSTRUINDO_VIA', 'OUTRO'
        ))
    );
