package com.camss.honeymoney.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El correo es obligatori")
    @Email(message = "El formato de correo es no valido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}
