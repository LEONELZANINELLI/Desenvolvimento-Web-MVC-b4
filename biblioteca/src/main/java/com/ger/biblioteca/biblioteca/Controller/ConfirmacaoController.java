
package com.ger.biblioteca.biblioteca.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Centraliza a entrada na biblioteca e a confirmação do cadastro de livro. */
@Controller
public class ConfirmacaoController {

    // GET / - Redireciona para a pagina inicial
    // GET /Livros - Corrige a rota antiga
    @GetMapping({"/", "/Livros"})
    public String redirecionarInicio() {
        return "redirect:/livros";
    }

    // O redirecionamento após o cadastro do livro aponta para esta rota GET.
    @GetMapping("/livros/confirmacao")
    public String confirmacao() {
        return "Livros/confirmacao";
    }
}
