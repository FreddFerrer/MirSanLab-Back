package com.mirsanlab.backend.repository;

import com.mirsanlab.backend.entity.Resultado;
import com.mirsanlab.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

    Page<Resultado> findByPaciente(Usuario paciente, Pageable pageable);

}
