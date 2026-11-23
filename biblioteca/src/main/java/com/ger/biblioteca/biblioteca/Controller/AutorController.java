
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Autor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

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
    public String salvar(@ModelAttribute("autor") Autor autor, BindingResult resultado) {
        validar(resultado);
        if (resultado.hasErrors()) {
            return "Livros/Autores/formularioautor";
        }
        autores.add(autor);
        return "redirect:/autor/confirmacao";
    }

    // GET /autor/editar/{indice} - Abre o formulario com os dados do autor
    @GetMapping("/editar/{indice}")
    public String editar(@PathVariable int indice, Model model) {
        verificarIndice(indice);
        model.addAttribute("autor", autores.get(indice));
        model.addAttribute("indice", indice);
        return "Livros/Autores/formularioautor";
    }

    // POST /autor/atualizar/{indice} - Salva as alteracoes do autor
    @PostMapping("/atualizar/{indice}")
    public String atualizar(@PathVariable int indice,
                            @ModelAttribute("autor") Autor autor,
                            BindingResult resultado, Model model) {
        verificarIndice(indice);
        validar(resultado);
        if (resultado.hasErrors()) {
            model.addAttribute("indice", indice);
            return "Livros/Autores/formularioautor";
        }
        autores.set(indice, autor);
        return "redirect:/autor";
    }

    // GET /autor/excluir/{indice} - Exclui o autor selecionado
    @GetMapping("/excluir/{indice}")
    public String excluir(@PathVariable int indice) {
        verificarIndice(indice);
        autores.remove(indice);
        return "redirect:/autor";
    }

    // GET /autor/confirmacao
    @GetMapping("/confirmacao")
    public String confirmacao() {
        return "Livros/Autores/confirmacaoautor";
    }

    private void verificarIndice(int indice) {
        if (indice < 0 || indice >= autores.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrado.");
        }
    }

    private void validar(BindingResult resultado) {
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "nome", "obrigatorio", "Informe o nome.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "descricao", "obrigatorio", "Informe a descrição.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "nacionalidade", "obrigatorio", "Informe a nacionalidade.");
    }

}
