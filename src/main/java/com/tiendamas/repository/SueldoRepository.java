package com.tiendamas.repository;

import com.tiendamas.entity.Sueldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SueldoRepository extends JpaRepository<Sueldo, Long> {
}
