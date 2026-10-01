package br.com.cidadeemfoco.exception;

public class WhatsappDeliveryException extends RuntimeException {

    public WhatsappDeliveryException(String message) {
        super(message);
    }

    public WhatsappDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
