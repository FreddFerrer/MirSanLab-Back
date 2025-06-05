package com.mirsanlab.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

// Registro (entrada del paciente)
public record UsuarioRegistroDto(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Email(message = "Email inválido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, max = 20, message = "La contraseña debe tener entre 4 y 20 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                message = "La contraseña debe contener al menos una mayúscula y un número"
        )
        String password,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono
) {}
