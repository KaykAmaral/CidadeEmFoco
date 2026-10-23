package br.com.cidadeemfoco.util;

import br.com.cidadeemfoco.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDataNormalizerTest {

    @Test
    void shouldNormalizeValidCpf() {
        assertThat(UserDataNormalizer.normalizeCpf("529.982.247-25"))
                .isEqualTo("52998224725");
    }

    @Test
    void shouldRejectCpfWithInvalidCheckDigits() {
        assertThatThrownBy(() -> UserDataNormalizer.normalizeCpf("529.982.247-24"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Informe um CPF valido");
    }

    @Test
    void shouldNormalizeBrazilianPhoneAndPostalCode() {
        assertThat(UserDataNormalizer.normalizePhone("(13) 99999-9999"))
                .isEqualTo("+5513999999999");
        assertThat(UserDataNormalizer.normalizePostalCode("11700-000"))
                .isEqualTo("11700000");
    }
}
