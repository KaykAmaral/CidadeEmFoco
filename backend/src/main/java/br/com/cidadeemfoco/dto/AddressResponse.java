package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.User;

public record AddressResponse(
        String street,
        String number,
        String complement,
        String neighborhood,
        String postalCode
) {
    public static AddressResponse from(User user) {
        if (user.getStreet() == null) {
            return null;
        }
        return new AddressResponse(
                user.getStreet(),
                user.getAddressNumber(),
                user.getAddressComplement(),
                user.getNeighborhood(),
                user.getPostalCode()
        );
    }
}
