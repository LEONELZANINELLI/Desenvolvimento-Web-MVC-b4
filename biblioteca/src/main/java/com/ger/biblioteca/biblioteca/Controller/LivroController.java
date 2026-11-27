
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Livro;
import com.ger.biblioteca.biblioteca.repository.LivroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Coordena o CRUD de livros e devolve as views usadas pelo Thymeleaf. */
@Controller
@RequestMapping("/livros")
public class LivroController {

    private final LivroRepository repository;

    public LivroController(LivroRepository repository) {
        this.repository = repository;
    }

    // A chave do Model é lida como ${livros} pelo template da listagem.
    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("livros", repository.findAll(Sort.by("id")));
        return "Livros/inicio";
    }

    // O template usa o objeto vazio como base para os campos do novo livro.
    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("livro", new Livro());
        return "Livros/formulario";
    }

    // O Spring vincula os campos ao Livro e reúne no BindingResult os erros do formulário.
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("livro") Livro livro, BindingResult resultado) {
        validar(resultado);
        if (resultado.hasErrors()) {
            return "Livros/formulario";
        }
        repository.save(livro);
        // Post/Redirect/Get faz o navegador abrir a confirmação em uma nova requisição GET.
        return "redirect:/livros/confirmacao";
    }

    // O ID do banco é estável mesmo depois de excluir registros anteriores.
    @GetMapping("/editar/{registroId}")
    public String editar(@PathVariable Long registroId, Model model) {
        model.addAttribute("livro", buscarLivro(registroId));
        model.addAttribute("registroId", registroId);
        return "Livros/formulario";
    }

    @Transactional
    @PostMapping("/atualizar/{registroId}")
    public String atualizar(@PathVariable Long registroId,
                            @ModelAttribute("livro") Livro livro,
                            BindingResult resultado, Model model) {
        Livro existente = buscarLivro(registroId);
        validar(resultado);
        if (resultado.hasErrors()) {
            // Preserva a URL de atualização ao mostrar os erros do formulário.
            model.addAttribute("registroId", registroId);
            return "Livros/formulario";
        }
        // Grava os campos editáveis na entidade carregada, mantendo a chave do banco.
        existente.setTitulo(livro.getTitulo());
        existente.setAutor(livro.getAutor());
        existente.setIsbn(livro.getIsbn());
        repository.save(existente);
        return "redirect:/livros";
    }

    // O link da tabela usa GET após a confirmação no navegador, como na aula.
    @Transactional
    @GetMapping("/excluir/{registroId}")
    public String excluir(@PathVariable Long registroId) {
        buscarLivro(registroId);
        repository.deleteById(registroId);
        return "redirect:/livros";
    }

    private Livro buscarLivro(Long registroId) {
        return repository.findById(registroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado."));
    }

    // A validação no servidor também rejeita campos preenchidos apenas com espaços.
    private void validar(BindingResult resultado) {
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "titulo", "obrigatorio", "Informe o título.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "autor", "obrigatorio", "Informe o autor.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "isbn", "obrigatorio", "Informe o ISBN.");
    }

}


