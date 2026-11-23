
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Livro;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public String salvar(@ModelAttribute("livro") Livro livro, BindingResult resultado) {
        validar(resultado);
        if (resultado.hasErrors()) {
            return "Livros/formulario";
        }
        livros.add(livro);
        return "redirect:/livros/confirmacao";
    }

    // GET /livros/editar/{indice} - Abre o formulario com os dados do livro
    @GetMapping("/editar/{indice}")
    public String editar(@PathVariable int indice, Model model) {
        verificarIndice(indice);
        model.addAttribute("livro", livros.get(indice));
        model.addAttribute("indice", indice);
        return "Livros/formulario";
    }

    // POST /livros/atualizar/{indice} - Atualiza sem adicionar outro livro
    @PostMapping("/atualizar/{indice}")
    public String atualizar(@PathVariable int indice,
                            @ModelAttribute("livro") Livro livro,
                            BindingResult resultado, Model model) {
        verificarIndice(indice);
        validar(resultado);
        if (resultado.hasErrors()) {
            model.addAttribute("indice", indice);
            return "Livros/formulario";
        }
        livros.set(indice, livro);
        return "redirect:/livros";
    }

    // adicionar exclusão de livros com Metodo GET
    @GetMapping("excluir/{indice}")
    public String excluir(@PathVariable int indice) {
        verificarIndice(indice);
        livros.remove(indice);
        return "redirect:/livros";
    }

    private void verificarIndice(int indice) {
        if (indice < 0 || indice >= livros.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado.");
        }
    }

    private void validar(BindingResult resultado) {
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "titulo", "obrigatorio", "Informe o título.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "autor", "obrigatorio", "Informe o autor.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "isbn", "obrigatorio", "Informe o ISBN.");
    }

}


