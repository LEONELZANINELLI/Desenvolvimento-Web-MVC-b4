package com.ger.biblioteca.biblioteca.form;

/** Campos enviados pelo formulário de livro. A seleção envia apenas o ID do autor. */
public class LivroFormulario {

    private String titulo;
    private Long autorId;
    private String isbn;

    public LivroFormulario() {
    }

    public LivroFormulario(String titulo, Long autorId, String isbn) {
        this.titulo = titulo;
        this.autorId = autorId;
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
