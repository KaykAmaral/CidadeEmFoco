package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.AuthResponse;
import br.com.cidadeemfoco.dto.LoginRequest;
import br.com.cidadeemfoco.dto.ForgotPasswordRequest;
import br.com.cidadeemfoco.dto.MessageResponse;
import br.com.cidadeemfoco.dto.RegisterRequest;
import br.com.cidadeemfoco.dto.ResetPasswordRequest;
import br.com.cidadeemfoco.service.AuthService;
import br.com.cidadeemfoco.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar um cidadao", security = {})
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar e obter um token JWT", security = {})
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperacao de senha", security = {})
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        return new MessageResponse("Se o email estiver cadastrado, enviaremos as instrucoes de recuperacao");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir a senha usando um token temporario", security = {})
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return new MessageResponse("Senha redefinida com sucesso");
    }
}
