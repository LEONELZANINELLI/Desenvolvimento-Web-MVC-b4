package com.ger.biblioteca.biblioteca.repository;

import com.ger.biblioteca.biblioteca.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}
