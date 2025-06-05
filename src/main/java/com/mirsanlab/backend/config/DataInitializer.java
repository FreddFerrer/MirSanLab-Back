package com.mirsanlab.backend.config;

import com.mirsanlab.backend.entity.Turno;
import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.repository.TurnoRepository;
import com.mirsanlab.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {

            // ADMIN
            usuarioRepository.findByEmail("admin@lab.com")
                    .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                            .nombre("Miriam Sandoval")
                            .email("admin@lab.com")
                            .password(passwordEncoder.encode("Admin123"))
                            .telefono("3624000000")
                            .rol(Usuario.Rol.ADMIN)
                            .build()));

            // Lista de pacientes con nombre/email/telefono
            List<Usuario> pacientes = new ArrayList<>();

            List<String[]> datos = List.of(
                    new String[]{"Juan Pérez", "juan.perez@gmail.com", "3624000001"},
                    new String[]{"Ana Gómez", "ana.gomez@gmail.com", "3624000002"},
                    new String[]{"Luis Martínez", "luis.martinez@gmail.com", "3624000003"},
                    new String[]{"Carla Rodríguez", "carla.rodriguez@gmail.com", "3624000004"},
                    new String[]{"Diego Fernández", "diego.fernandez@gmail.com", "3624000005"},
                    new String[]{"Lucía Herrera", "lucia.herrera@gmail.com", "3624000006"},
                    new String[]{"Matías López", "matias.lopez@gmail.com", "3624000007"},
                    new String[]{"Camila Torres", "camila.torres@gmail.com", "3624000008"},
                    new String[]{"Sofía Méndez", "sofia.mendez@gmail.com", "3624000009"},
                    new String[]{"Fernando Ruiz", "fernando.ruiz@gmail.com", "3624000010"}
            );

            for (String[] d : datos) {
                String nombre = d[0];
                String email = d[1];
                String telefono = d[2];

                Usuario paciente = usuarioRepository.findByEmail(email)
                        .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                                .nombre(nombre)
                                .email(email)
                                .password(passwordEncoder.encode("Paciente123"))
                                .telefono(telefono)
                                .rol(Usuario.Rol.PACIENTE)
                                .build()));

                pacientes.add(paciente);
            }

            // Crear 3 turnos si no existen
            if (turnoRepository.count() == 0 && pacientes.size() >= 3) {
                turnoRepository.saveAll(List.of(
                        Turno.builder()
                                .fecha(LocalDate.now().plusDays(1))
                                .hora(LocalTime.of(8, 0))
                                .paciente(pacientes.get(0))
                                .estado(Turno.Estado.PENDIENTE)
                                .creadoEn(LocalDateTime.now())
                                .build(),
                        Turno.builder()
                                .fecha(LocalDate.now().plusDays(2))
                                .hora(LocalTime.of(9, 0))
                                .paciente(pacientes.get(1))
                                .estado(Turno.Estado.PENDIENTE)
                                .creadoEn(LocalDateTime.now())
                                .build(),
                        Turno.builder()
                                .fecha(LocalDate.now().plusDays(3))
                                .hora(LocalTime.of(10, 30))
                                .paciente(pacientes.get(2))
                                .estado(Turno.Estado.PENDIENTE)
                                .creadoEn(LocalDateTime.now())
                                .build()
                ));
            }

            System.out.println("✅ Usuarios y turnos de prueba insertados correctamente.");
        };
    }
}

