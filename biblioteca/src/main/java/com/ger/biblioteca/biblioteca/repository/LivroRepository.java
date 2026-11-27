package com.ger.biblioteca.biblioteca.repository;

import java.util.List;
import java.util.Optional;

import com.ger.biblioteca.biblioteca.model.Livro;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    @Override
    @EntityGraph(attributePaths = "autorCadastrado")
    List<Livro> findAll(Sort sort);

    @Override
    @EntityGraph(attributePaths = "autorCadastrado")
    Optional<Livro> findById(Long id);
}
