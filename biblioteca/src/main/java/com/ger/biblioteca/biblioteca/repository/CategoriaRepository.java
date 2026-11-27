package com.ger.biblioteca.biblioteca.repository;

import com.ger.biblioteca.biblioteca.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
