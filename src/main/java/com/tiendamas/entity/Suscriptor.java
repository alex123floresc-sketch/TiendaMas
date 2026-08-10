package com.tiendamas.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "suscriptor", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Suscriptor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private LocalDateTime fechaSuscripcion = LocalDateTime.now();

    public Suscriptor() {}

    public Suscriptor(String email) {
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getFechaSuscripcion() { return fechaSuscripcion; }
    public void setFechaSuscripcion(LocalDateTime fechaSuscripcion) { this.fechaSuscripcion = fechaSuscripcion; }
}
