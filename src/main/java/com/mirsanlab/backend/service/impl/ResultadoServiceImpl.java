package com.mirsanlab.backend.service.impl;

import com.mirsanlab.backend.dto.ResultadoResponseDto;
import com.mirsanlab.backend.entity.Resultado;
import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.exceptions.ArchivoInvalidoException;
import com.mirsanlab.backend.exceptions.CorreoInvalidoException;
import com.mirsanlab.backend.exceptions.EnvioCorreoException;
import com.mirsanlab.backend.exceptions.ResultadoNoEncontradoException;
import com.mirsanlab.backend.exceptions.UsuarioNoEncontradoException;
import com.mirsanlab.backend.mapper.ResultadoMapper;
import com.mirsanlab.backend.repository.ResultadoRepository;
import com.mirsanlab.backend.repository.UsuarioRepository;
import com.mirsanlab.backend.service.ResultadoService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ResultadoServiceImpl implements ResultadoService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );
    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioRepository usuarioRepository;
    private final ResultadoRepository resultadoRepository;
    private final ResultadoMapper resultadoMapper;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${mail.brand.name:MirsanLab}")
    private String nombreLaboratorio;

    @Value("${mail.contact.email:}")
    private String mailContacto;

    @Value("${mail.contact.phone:}")
    private String telefonoContacto;

    @Override
    public void subirResultado(Long pacienteId, MultipartFile archivo) {
        Usuario paciente = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(pacienteId));

        procesarSubidaYEnvio(paciente, paciente.getEmail(), archivo);
    }

    @Override
    public void subirResultadoPorEmail(String emailDestino, MultipartFile archivo) {
        String emailNormalizado = normalizarYValidarEmail(emailDestino);
        procesarSubidaYEnvioPorEmail(emailNormalizado, archivo);
    }

    @Override
    public Page<ResultadoResponseDto> obtenerResultadosPaciente(Long pacienteId, int page, int size) {
        Usuario paciente = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(pacienteId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("creadoEn").descending());

        return resultadoRepository.findByPaciente(paciente, pageable)
                .map(resultadoMapper::toDto);
    }

    @Override
    public ResponseEntity<Resource> descargarResultado(Long resultadoId, Usuario paciente) {
        Resultado resultado = resultadoRepository.findById(resultadoId)
                .orElseThrow(() -> new ResultadoNoEncontradoException(resultadoId));

        if (!resultado.getPaciente().getId().equals(paciente.getId())) {
            throw new AccessDeniedException("No tenes permiso para acceder a este archivo.");
        }

        Path archivoPath = Paths.get(resultado.getArchivoUrl());

        if (!Files.exists(archivoPath)) {
            throw new ResultadoNoEncontradoException(resultadoId);
        }

        Resource recurso = new FileSystemResource(archivoPath);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + archivoPath.getFileName() + "\"")
                .body(recurso);
    }

    private void procesarSubidaYEnvio(Usuario paciente, String emailDestino, MultipartFile archivo) {
        validarArchivoPdf(archivo);
        Path path = guardarArchivo(paciente.getId(), archivo);
        Resultado resultadoGuardado = null;

        try {
            resultadoGuardado = guardarResultado(paciente, path);
            enviarCorreoConAdjunto(
                    emailDestino,
                    path,
                    paciente.getNombre(),
                    resultadoGuardado.getCreadoEn(),
                    resultadoGuardado.getId()
            );
        } catch (RuntimeException ex) {
            eliminarResultadoSilencioso(resultadoGuardado);
            eliminarArchivoSilencioso(path);
            throw ex;
        }
    }

    private void procesarSubidaYEnvioPorEmail(String emailDestino, MultipartFile archivo) {
        validarArchivoPdf(archivo);
        Path path = guardarArchivo(0L, archivo);

        try {
            Optional<Usuario> paciente = usuarioRepository.findByEmailIgnoreCase(emailDestino);
            Resultado resultadoGuardado = paciente
                    .map(usuario -> guardarResultado(usuario, path))
                    .orElse(null);

            enviarCorreoConAdjunto(
                    emailDestino,
                    path,
                    paciente.map(Usuario::getNombre).orElse(null),
                    resultadoGuardado != null ? resultadoGuardado.getCreadoEn() : null,
                    resultadoGuardado != null ? resultadoGuardado.getId() : null
            );

            if (paciente.isEmpty()) {
                eliminarArchivoSilencioso(path);
            }
        } catch (RuntimeException ex) {
            eliminarArchivoSilencioso(path);
            throw ex;
        }
    }

    private String normalizarYValidarEmail(String emailDestino) {
        String email = emailDestino == null ? "" : emailDestino.trim().toLowerCase();
        if (!StringUtils.hasText(email)) {
            throw new CorreoInvalidoException("Debe ingresar un correo de destino.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new CorreoInvalidoException("El correo de destino no es valido.");
        }
        return email;
    }

    private void validarArchivoPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoInvalidoException("Debe enviar un archivo PDF.");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String contentType = archivo.getContentType();

        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".pdf")) {
            throw new ArchivoInvalidoException("El archivo debe tener extension .pdf.");
        }

        if (!MediaType.APPLICATION_PDF_VALUE.equals(contentType)) {
            throw new ArchivoInvalidoException("El archivo debe ser de tipo PDF (application/pdf).");
        }
    }

    private Path guardarArchivo(Long pacienteId, MultipartFile archivo) {
        String nombreArchivo = "resultado_" + pacienteId + "_" + System.currentTimeMillis() + ".pdf";
        Path path = Paths.get("archivos", nombreArchivo);

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, archivo.getBytes());
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo PDF.", e);
        }
    }

    private Resultado guardarResultado(Usuario paciente, Path path) {
        Resultado resultado = Resultado.builder()
                .archivoUrl(path.toString())
                .paciente(paciente)
                .creadoEn(LocalDateTime.now())
                .build();

        return resultadoRepository.save(resultado);
    }

    private void enviarCorreoConAdjunto(
            String emailDestino,
            Path archivo,
            String nombrePaciente,
            LocalDateTime fechaEmision,
            Long resultadoId) {

        MimeMessage mensaje = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            if (StringUtils.hasText(mailFrom)) {
                helper.setFrom(mailFrom);
            }
            helper.setTo(emailDestino);
            helper.setSubject(nombreLaboratorio + " | Resultado de laboratorio disponible");
            helper.setText(construirHtmlEmail(nombrePaciente, fechaEmision, resultadoId), true);
            helper.addAttachment(archivo.getFileName().toString(), archivo.toFile());

            mailSender.send(mensaje);
        } catch (MessagingException | MailException ex) {
            throw new EnvioCorreoException("No se pudo enviar el correo con el resultado.", ex);
        }
    }

    private String construirHtmlEmail(String nombrePaciente, LocalDateTime fechaEmision, Long resultadoId) {
        String nombre = StringUtils.hasText(nombrePaciente) ? escapeHtml(nombrePaciente) : null;
        String fecha = fechaEmision != null ? FECHA_FORMATTER.format(fechaEmision) : null;
        String resultado = resultadoId != null ? String.valueOf(resultadoId) : null;

        boolean mostrarNombre = nombre != null;
        boolean mostrarFecha = fecha != null;
        boolean mostrarId = resultado != null;
        boolean mostrarBloqueDatos = mostrarNombre || mostrarFecha || mostrarId;
        boolean mostrarContacto = StringUtils.hasText(mailContacto) || StringUtils.hasText(telefonoContacto);

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>")
                .append("<html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>Resultado de laboratorio disponible</title></head>")
                .append("<body style=\"margin:0;padding:0;background-color:#F8FFFB;font-family:'Segoe UI',Tahoma,Arial,sans-serif;color:#263138;\">")
                .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color:#F8FFFB;padding:24px 12px;\">")
                .append("<tr><td align=\"center\">")
                .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:600px;background-color:#FFFFFF;border:1px solid #E5E5E5;border-radius:16px;box-shadow:0 2px 8px rgba(38,49,56,0.06);overflow:hidden;\">")
                .append("<tr><td style=\"background-color:#F0FDFA;padding:20px 24px;border-bottom:1px solid #E5E5E5;\">")
                .append("<div style=\"font-size:22px;font-weight:700;color:#263138;line-height:1.3;\">")
                .append(escapeHtml(nombreLaboratorio))
                .append("</div>")
                .append("<div style=\"font-size:13px;color:#6B7280;margin-top:4px;\">Laboratorio de analisis clinicos</div>")
                .append("</td></tr>")
                .append("<tr><td style=\"padding:28px 24px 16px 24px;\">")
                .append("<h1 style=\"margin:0 0 12px 0;font-size:24px;line-height:1.3;color:#263138;\">Resultado de laboratorio disponible</h1>")
                .append("<p style=\"margin:0 0 10px 0;font-size:16px;line-height:1.6;color:#444747;\">")
                .append("Hemos cargado su resultado de laboratorio y lo adjuntamos en formato PDF en este correo.")
                .append("</p>")
                .append("<p style=\"margin:0;font-size:15px;line-height:1.6;color:#6B7280;\">")
                .append("Por favor, conserve este mensaje y contacte al laboratorio ante cualquier consulta.")
                .append("</p>")
                .append("</td></tr>");

        if (mostrarBloqueDatos) {
            html.append("<tr><td style=\"padding:0 24px 8px 24px;\">")
                    .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border:1px solid #E5E5E5;border-radius:12px;background-color:#FFFFFF;\">");

            if (mostrarNombre) {
                html.append("<tr><td style=\"padding:12px 14px;border-bottom:")
                        .append((mostrarFecha || mostrarId) ? "1px solid #E5E5E5" : "0")
                        .append(";font-size:14px;color:#444747;\">")
                        .append("<strong style=\"color:#263138;\">Paciente:</strong> ")
                        .append(nombre)
                        .append("</td></tr>");
            }
            if (mostrarFecha) {
                html.append("<tr><td style=\"padding:12px 14px;border-bottom:")
                        .append(mostrarId ? "1px solid #E5E5E5" : "0")
                        .append(";font-size:14px;color:#444747;\">")
                        .append("<strong style=\"color:#263138;\">Fecha de emision:</strong> ")
                        .append(escapeHtml(fecha))
                        .append("</td></tr>");
            }
            if (mostrarId) {
                html.append("<tr><td style=\"padding:12px 14px;font-size:14px;color:#444747;\">")
                        .append("<strong style=\"color:#263138;\">ID de resultado:</strong> #")
                        .append(escapeHtml(resultado))
                        .append("</td></tr>");
            }

            html.append("</table></td></tr>");
        }

        html.append("<tr><td style=\"padding:18px 24px 24px 24px;background-color:#FFFFFF;border-top:1px solid #E5E5E5;\">")
                .append("<p style=\"margin:0 0 8px 0;font-size:13px;line-height:1.6;color:#6B7280;\">")
                .append("Este correo contiene informacion confidencial de salud. Si lo recibio por error, por favor eliminelo.")
                .append("</p>");

        if (mostrarContacto) {
            html.append("<p style=\"margin:0;font-size:13px;line-height:1.6;color:#6B7280;\">Contacto ");
            if (StringUtils.hasText(mailContacto)) {
                html.append(escapeHtml(mailContacto));
            }
            if (StringUtils.hasText(mailContacto) && StringUtils.hasText(telefonoContacto)) {
                html.append(" | ");
            }
            if (StringUtils.hasText(telefonoContacto)) {
                html.append(escapeHtml(telefonoContacto));
            }
            html.append("</p>");
        }

        html.append("</td></tr></table></td></tr></table></body></html>");
        return html.toString();
    }

    private void eliminarArchivoSilencioso(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // No afecta la respuesta principal.
        }
    }

    private void eliminarResultadoSilencioso(Resultado resultado) {
        if (resultado == null || resultado.getId() == null) {
            return;
        }
        try {
            resultadoRepository.deleteById(resultado.getId());
        } catch (RuntimeException ignored) {
            // No afecta la respuesta principal.
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
