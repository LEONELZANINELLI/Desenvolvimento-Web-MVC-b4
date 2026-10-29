
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Autor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/autor")
public class AutorController {

    private final List<Autor> autores = new ArrayList<>();

    // GET /autor - Lista os autores cadastrados
    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("autores", autores);
        return "Livros/Autores/inicioautor";
    }

    // GET /autor/novo - Abre o formulario
    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("autor", new Autor());
        return "Livros/Autores/formularioautor";
    }


    // POST /autor/salvar
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("autor") Autor autor) {
        autores.add(autor);
        return "redirect:/autor/confirmacao";
    }

    // GET /autor/confirmacao
    @GetMapping("/confirmacao")
    public String confirmacao() {
        return "Livros/Autores/confirmacaoautor";
    }

}
