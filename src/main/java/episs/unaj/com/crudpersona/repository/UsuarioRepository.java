package episs.unaj.com.crudpersona.repository;

import episs.unaj.com.crudpersona.entity.RolUsuario;
import episs.unaj.com.crudpersona.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Usuario> findByRolIn(List<RolUsuario> roles);
}
