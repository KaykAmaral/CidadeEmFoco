package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.UpdateWhatsappPreferencesRequest;
import br.com.cidadeemfoco.dto.UpdateUserProfileRequest;
import br.com.cidadeemfoco.dto.UserProfileResponse;
import br.com.cidadeemfoco.dto.WhatsappPreferencesResponse;
import br.com.cidadeemfoco.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserProfileResponse findProfile(Authentication authentication) {
        return userService.findProfile(authentication.getName());
    }

    @PutMapping
    public UserProfileResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userService.updateProfile(authentication.getName(), request);
    }

    @GetMapping("/whatsapp")
    public WhatsappPreferencesResponse findPreferences(Authentication authentication) {
        return userService.findWhatsappPreferences(authentication.getName());
    }

    @PutMapping("/whatsapp")
    public WhatsappPreferencesResponse enableNotifications(
            Authentication authentication,
            @Valid @RequestBody UpdateWhatsappPreferencesRequest request
    ) {
        return userService.enableWhatsappNotifications(authentication.getName(), request);
    }

    @DeleteMapping("/whatsapp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableNotifications(Authentication authentication) {
        userService.disableWhatsappNotifications(authentication.getName());
    }
}
