
package com.ger.biblioteca.biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Entidade de livro vinculada ao cadastro de autores. */
@Entity
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    // Mantém o texto de livros gravados antes do vínculo com o cadastro de autores.
    @Column(name = "autor")
    private String autorLegado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Autor autorCadastrado;

    // Guarda o identificador como texto para preservar sua representação.
    private String isbn;

    public Livro() {
    }

    public Livro(String titulo, Autor autorCadastrado, String isbn) {
        this.titulo = titulo;
        this.autorCadastrado = autorCadastrado;
        this.isbn = isbn;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autorCadastrado == null ? autorLegado : autorCadastrado.getNome();
    }

    public Autor getAutorCadastrado() {
        return autorCadastrado;
    }

    public void setAutorCadastrado(Autor autorCadastrado) {
        this.autorCadastrado = autorCadastrado;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
