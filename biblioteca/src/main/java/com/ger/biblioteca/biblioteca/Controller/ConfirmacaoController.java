
package com.ger.biblioteca.biblioteca.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfirmacaoController {

    // GET / - Redireciona para a pagina inicial
    // GET /Livros - Corrige a rota antiga
    @GetMapping({"/", "/Livros"})
    public String redirecionarInicio() {
        return "redirect:/livros";
    }

    // GET /livros/confirmacao
    @GetMapping("/livros/confirmacao")
    public String confirmacao() {
        return "Livros/confirmacao";
    }
}
