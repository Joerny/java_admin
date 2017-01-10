package com.joerny.example.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildEntityRepository extends JpaRepository<ChildEntity, Long> {
}
