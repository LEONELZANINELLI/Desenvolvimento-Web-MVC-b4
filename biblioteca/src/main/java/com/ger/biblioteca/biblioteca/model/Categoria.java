package com.ger.biblioteca.biblioteca.model;

/** Modelo do formulário de categoria com ID informado pelo usuário. */
public class Categoria {
    // A URL de edição usa a posição na lista. Este campo guarda o ID mostrado na tabela.
    private int id;
    private String nome;

    public Categoria() {}

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

}


