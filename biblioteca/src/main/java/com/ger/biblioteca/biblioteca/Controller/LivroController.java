
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.form.LivroFormulario;
import com.ger.biblioteca.biblioteca.model.Autor;
import com.ger.biblioteca.biblioteca.model.Livro;
import com.ger.biblioteca.biblioteca.repository.AutorRepository;
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
    private final AutorRepository autorRepository;

    public LivroController(LivroRepository repository, AutorRepository autorRepository) {
        this.repository = repository;
        this.autorRepository = autorRepository;
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
        model.addAttribute("livro", new LivroFormulario());
        return mostrarFormulario(model);
    }

    // O formulário envia o ID do autor. O controller busca a entidade antes de salvar.
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("livro") LivroFormulario livro, BindingResult resultado, Model model) {
        Autor autor = validar(livro, resultado, false);
        if (resultado.hasErrors()) {
            return mostrarFormulario(model);
        }
        repository.save(new Livro(livro.getTitulo(), autor, livro.getIsbn()));
        // Post/Redirect/Get faz o navegador abrir a confirmação em uma nova requisição GET.
        return "redirect:/livros/confirmacao";
    }

    // O ID do banco é estável mesmo depois de excluir registros anteriores.
    @GetMapping("/editar/{registroId}")
    public String editar(@PathVariable Long registroId, Model model) {
        Livro livro = buscarLivro(registroId);
        Autor autor = livro.getAutorCadastrado();
        Long autorId = autor == null ? null : autor.getId();
        model.addAttribute("livro", new LivroFormulario(livro.getTitulo(), autorId, livro.getIsbn()));
        return mostrarEdicao(livro, model);
    }

    @Transactional
    @PostMapping("/atualizar/{registroId}")
    public String atualizar(@PathVariable Long registroId,
                            @ModelAttribute("livro") LivroFormulario livro,
                            BindingResult resultado, Model model) {
        Livro existente = buscarLivro(registroId);
        Autor autor = validar(livro, resultado, autorAnterior(existente) != null);
        if (resultado.hasErrors()) {
            return mostrarEdicao(existente, model);
        }
        // Grava os campos editáveis na entidade carregada, mantendo a chave do banco.
        existente.setTitulo(livro.getTitulo());
        existente.setAutorCadastrado(autor);
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

    private String mostrarEdicao(Livro livro, Model model) {
        model.addAttribute("registroId", livro.getId());
        model.addAttribute("autorAnterior", autorAnterior(livro));
        return mostrarFormulario(model);
    }

    private String mostrarFormulario(Model model) {
        model.addAttribute("autores", autorRepository.findAll(Sort.by("nome", "id")));
        return "Livros/formulario";
    }

    private String autorAnterior(Livro livro) {
        return livro.getAutorCadastrado() == null ? livro.getAutor() : null;
    }

    private Autor validar(LivroFormulario livro, BindingResult resultado, boolean podeManterAutorAnterior) {
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "titulo", "obrigatorio", "Informe o título.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "isbn", "obrigatorio", "Informe o ISBN.");

        if (resultado.hasFieldErrors("autorId")) {
            return null;
        }
        if (livro.getAutorId() == null) {
            if (!podeManterAutorAnterior) {
                resultado.rejectValue("autorId", "obrigatorio", "Selecione um autor cadastrado.");
            }
            return null;
        }

        Autor autor = autorRepository.findById(livro.getAutorId()).orElse(null);
        if (autor == null) {
            resultado.rejectValue("autorId", "invalido", "O autor selecionado não está cadastrado.");
        }
        return autor;
    }

}


