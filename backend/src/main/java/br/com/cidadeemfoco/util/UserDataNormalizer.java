package br.com.cidadeemfoco.util;

import br.com.cidadeemfoco.exception.BusinessRuleException;

import java.util.regex.Pattern;

public final class UserDataNormalizer {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private UserDataNormalizer() {
    }

    public static String normalizeCpf(String value) {
        String cpf = value.replaceAll("\\D", "");
        if (!isValidCpf(cpf)) {
            throw new BusinessRuleException("Informe um CPF valido");
        }
        return cpf;
    }

    public static String normalizePhone(String value) {
        String trimmed = value.trim();
        String digits = trimmed.replaceAll("\\D", "");
        String normalized;

        if (trimmed.startsWith("+")) {
            normalized = "+" + digits;
        } else if ((digits.length() == 12 || digits.length() == 13) && digits.startsWith("55")) {
            normalized = "+" + digits;
        } else if (digits.length() == 10 || digits.length() == 11) {
            normalized = "+55" + digits;
        } else {
            throw new BusinessRuleException("Informe um numero de WhatsApp valido com DDD");
        }

        if (!E164_PATTERN.matcher(normalized).matches()) {
            throw new BusinessRuleException("Informe um numero de WhatsApp valido no formato internacional");
        }
        return normalized;
    }

    public static String normalizePostalCode(String value) {
        String postalCode = value.replaceAll("\\D", "");
        if (postalCode.length() != 8) {
            throw new BusinessRuleException("Informe um CEP valido");
        }
        return postalCode;
    }

    private static boolean isValidCpf(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return calculateCpfDigit(cpf, 9) == Character.getNumericValue(cpf.charAt(9))
                && calculateCpfDigit(cpf, 10) == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calculateCpfDigit(String cpf, int digitPosition) {
        int sum = 0;
        int weight = digitPosition + 1;
        for (int index = 0; index < digitPosition; index++) {
            sum += Character.getNumericValue(cpf.charAt(index)) * (weight - index);
        }
        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }
}
