package br.com.cidadeemfoco.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OccurrenceCategoryTest {

    @Test
    void shouldAcceptOnlyTypesThatBelongToTheCategory() {
        assertThat(OccurrenceCategory.EVENTO_NATURAL.allows(OccurrenceType.ALAGAMENTO)).isTrue();
        assertThat(OccurrenceCategory.EVENTO_NATURAL.allows(OccurrenceType.BURACO_RUA)).isFalse();
        assertThat(OccurrenceCategory.INFRAESTRUTURA_URBANA.allows(OccurrenceType.BURACO_RUA)).isTrue();
        assertThat(OccurrenceCategory.INFRAESTRUTURA_URBANA.allows(OccurrenceType.ENCHENTE)).isFalse();
    }

    @Test
    void shouldAcceptOtherForBothCategories() {
        assertThat(OccurrenceCategory.EVENTO_NATURAL.allows(OccurrenceType.OUTRO)).isTrue();
        assertThat(OccurrenceCategory.INFRAESTRUTURA_URBANA.allows(OccurrenceType.OUTRO)).isTrue();
    }
}

