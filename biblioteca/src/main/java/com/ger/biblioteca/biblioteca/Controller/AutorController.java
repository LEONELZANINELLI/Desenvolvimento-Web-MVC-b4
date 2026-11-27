
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Autor;
import com.ger.biblioteca.biblioteca.repository.AutorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Coordena o CRUD de autores e exibe a confirmação após um cadastro válido. */
@Controller
@RequestMapping("/autor")
public class AutorController {

    private final AutorRepository repository;

    public AutorController(AutorRepository repository) {
        this.repository = repository;
    }

    // GET /autor - Lista os autores cadastrados
    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("autores", repository.findAll(Sort.by("id")));
        return "Livros/Autores/inicioautor";
    }

    // GET /autor/novo - Abre o formulario
    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("autor", new Autor());
        return "Livros/Autores/formularioautor";
    }

    // O formulário é renderizado novamente com o BindingResult quando a validação falha.
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("autor") Autor autor, BindingResult resultado) {
        validar(resultado);
        if (resultado.hasErrors()) {
            return "Livros/Autores/formularioautor";
        }
        repository.save(autor);
        // A página de confirmação é aberta por GET depois do POST de cadastro.
        return "redirect:/autor/confirmacao";
    }

    // O ID do banco é estável mesmo depois de excluir registros anteriores.
    @GetMapping("/editar/{registroId}")
    public String editar(@PathVariable Long registroId, Model model) {
        model.addAttribute("autor", buscarAutor(registroId));
        model.addAttribute("registroId", registroId);
        return "Livros/Autores/formularioautor";
    }

    @Transactional
    @PostMapping("/atualizar/{registroId}")
    public String atualizar(@PathVariable Long registroId,
                            @ModelAttribute("autor") Autor autor,
                            BindingResult resultado, Model model) {
        Autor existente = buscarAutor(registroId);
        validar(resultado);
        if (resultado.hasErrors()) {
            // Preserva a URL de atualização ao mostrar os erros do formulário.
            model.addAttribute("registroId", registroId);
            return "Livros/Autores/formularioautor";
        }
        // Grava os campos editáveis na entidade carregada, mantendo a chave do banco.
        existente.setNome(autor.getNome());
        existente.setDescricao(autor.getDescricao());
        existente.setNacionalidade(autor.getNacionalidade());
        repository.save(existente);
        return "redirect:/autor";
    }

    // O link da tabela usa GET após a confirmação no navegador, como na aula.
    @GetMapping("/excluir/{registroId}")
    public String excluir(@PathVariable Long registroId, RedirectAttributes redirect) {
        buscarAutor(registroId);
        try {
            repository.deleteById(registroId);
        } catch (DataIntegrityViolationException exception) {
            // A chave estrangeira protege os livros que ainda usam este autor.
            redirect.addFlashAttribute("erroExclusao",
                    "Este autor está vinculado a um livro. Altere o autor no livro antes de excluir.");
        }
        return "redirect:/autor";
    }

    // GET /autor/confirmacao
    @GetMapping("/confirmacao")
    public String confirmacao() {
        return "Livros/Autores/confirmacaoautor";
    }

    private Autor buscarAutor(Long registroId) {
        return repository.findById(registroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrado."));
    }

    private void validar(BindingResult resultado) {
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "nome", "obrigatorio", "Informe o nome.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "descricao", "obrigatorio", "Informe a descrição.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "nacionalidade", "obrigatorio", "Informe a nacionalidade.");
    }

}
