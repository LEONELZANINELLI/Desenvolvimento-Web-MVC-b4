
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Livro;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/livros")
public class LivroController {

    private final List<Livro> livros = new ArrayList<>();

    // GET /livros - Lista os livros
    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("livros", livros);
        return "Livros/inicio";
    }

    // GET /livros/novo - Abre o formulario
    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("livro", new Livro());
        return "Livros/formulario";
    }

    // POST /livros/salvar - Salva o livro
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("livro") Livro livro) {
        livros.add(livro);
        return "redirect:/livros/confirmacao";
    }
}
