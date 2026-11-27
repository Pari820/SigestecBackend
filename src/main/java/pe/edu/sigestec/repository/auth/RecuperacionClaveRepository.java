package pe.edu.sigestec.repository.auth;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import pe.edu.sigestec.entity.auth.RecuperacionClave;

public interface RecuperacionClaveRepository extends JpaRepository<RecuperacionClave, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RecuperacionClave r WHERE r.tokenHash = :hash")
    Optional<RecuperacionClave> bloquearPorHash(@Param("hash") String hash);

    long countByUsuarioIdAndFechaCreacionAfter(Long usuarioId, LocalDateTime desde);

    @Modifying
    @Query("UPDATE RecuperacionClave r SET r.fechaUso = :ahora " +
           "WHERE r.usuario.id = :usuarioId AND r.fechaUso IS NULL")
    int invalidarPendientes(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);
}
