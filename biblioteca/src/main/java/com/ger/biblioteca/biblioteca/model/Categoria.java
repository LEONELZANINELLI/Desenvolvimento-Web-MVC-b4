package com.ger.biblioteca.biblioteca.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Entidade do cadastro de categorias com código informado no formulário. */
@Entity
public class Categoria {

    // Chave gerada pelo banco, usada nas URLs de edição e exclusão.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registroId;

    // ID editável que aparece no formulário e na tabela de categorias.
    private int id;
    private String nome;

    public Categoria() {}

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getRegistroId() {
        return registroId;
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


