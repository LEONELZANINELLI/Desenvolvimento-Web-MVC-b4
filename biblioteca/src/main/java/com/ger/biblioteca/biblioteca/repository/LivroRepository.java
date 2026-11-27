package com.ger.biblioteca.biblioteca.repository;

import com.ger.biblioteca.biblioteca.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {
}
