package com.tiendamas.repository;

import com.tiendamas.entity.Suscriptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuscriptorRepository extends JpaRepository<Suscriptor, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
