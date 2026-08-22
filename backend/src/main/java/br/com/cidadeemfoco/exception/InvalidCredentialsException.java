package br.com.cidadeemfoco.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email ou senha invalidos");
    }
}
