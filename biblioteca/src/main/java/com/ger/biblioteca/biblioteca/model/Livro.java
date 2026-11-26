
package com.ger.biblioteca.biblioteca.model;

/** Modelo do formulário de livro. O campo autor guarda o texto digitado pelo usuário. */
public class Livro {

    private String titulo;
    private String autor;
    // Guarda o identificador como texto para preservar sua representação.
    private String isbn;

    public Livro() {
    }

    public Livro(String titulo, String autor, String isbn) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
