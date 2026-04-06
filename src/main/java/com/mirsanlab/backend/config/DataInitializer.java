package com.mirsanlab.backend.config;

import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bootstrap.admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${bootstrap.admin.email:}")
    private String bootstrapAdminEmail;

    @Value("${bootstrap.admin.password:}")
    private String bootstrapAdminPassword;

    @Value("${bootstrap.admin.name:Administrador}")
    private String bootstrapAdminName;

    @Value("${bootstrap.admin.phone:}")
    private String bootstrapAdminPhone;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            if (!bootstrapAdminEnabled || usuarioRepository.existsByRol(Usuario.Rol.ADMIN)) {
                return;
            }

            if (!StringUtils.hasText(bootstrapAdminEmail) || !StringUtils.hasText(bootstrapAdminPassword)) {
                throw new IllegalStateException(
                        "Bootstrap admin habilitado, pero faltan bootstrap.admin.email o bootstrap.admin.password."
                );
            }

            Usuario admin = Usuario.builder()
                    .nombre(bootstrapAdminName)
                    .email(bootstrapAdminEmail.trim().toLowerCase())
                    .password(passwordEncoder.encode(bootstrapAdminPassword))
                    .telefono(StringUtils.hasText(bootstrapAdminPhone) ? bootstrapAdminPhone.trim() : null)
                    .rol(Usuario.Rol.ADMIN)
                    .build();

            usuarioRepository.save(admin);
        };
    }
}
