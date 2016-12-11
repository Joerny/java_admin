package com.joerny.example.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BasicEntityRepository extends JpaRepository<BasicEntity, Long> {
}
