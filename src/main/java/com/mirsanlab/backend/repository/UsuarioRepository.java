package com.mirsanlab.backend.repository;

import com.mirsanlab.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    @Query("""
    SELECT u FROM Usuario u
    WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
       OR u.telefono LIKE CONCAT('%', :query, '%')
    """)
    List<Usuario> buscarPorNombreEmailOTelefono(@Param("query") String query);

}
