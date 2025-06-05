package com.mirsanlab.backend.repository;

import com.mirsanlab.backend.entity.Turno;
import com.mirsanlab.backend.entity.Usuario;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByPaciente(Usuario paciente);

    List<Turno> findByFecha(LocalDate fecha); // Para que el bioquímico vea los del día

    List<Turno> findByPacienteAndFecha(Usuario paciente, LocalDate fecha);
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);
    List<Turno> findByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraAsc(LocalDate fecha, Turno.Estado estado);
    List<Turno> findByFechaAndEstado(LocalDate fecha, Turno.Estado estado);
    List<Turno> findByEstado(Turno.Estado estado);

    @Query("""
    SELECT t FROM Turno t
    WHERE t.paciente.id = :pacienteId
      AND t.estado = 'PENDIENTE'
      AND (t.fecha > :hoy OR (t.fecha = :hoy AND t.hora > :ahora))
    ORDER BY t.fecha ASC, t.hora ASC
    """)
    List<Turno> findProximoTurno(Long pacienteId, LocalDate hoy, LocalTime ahora, PageRequest pageable);


}