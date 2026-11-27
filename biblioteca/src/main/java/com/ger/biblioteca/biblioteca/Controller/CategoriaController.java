
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Categoria;
import com.ger.biblioteca.biblioteca.repository.CategoriaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Coordena o CRUD de categorias com validação do ID recebido pelo formulário. */
@Controller
@RequestMapping("/livros/categorias")
public class CategoriaController {

    private final CategoriaRepository repository;

    public CategoriaController(CategoriaRepository repository) {
        this.repository = repository;
    }

    // GET - Exibir formulario de cadastro
    @GetMapping("/categoriaFormulario")
    public String exibirFormulario(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "Livros/Categorias/categoriaFormulario";
    }

    // POST - Cadastrar nova categoria
    @PostMapping("/salvar")
    public String cadastrarCategoria(
            @ModelAttribute("categoria") Categoria categoria,
            BindingResult resultado,
            Model model) {

        validar(categoria, resultado);
        if (resultado.hasErrors()) {
            return "Livros/Categorias/categoriaFormulario";
        }
        repository.save(categoria);

        model.addAttribute("categoria", categoria);

        // O POST renderiza a confirmação com os dados que acabaram de ser cadastrados.
        return "Livros/Categorias/confirmacaoCategoria";
    }

    // GET - Listar todas as categorias
    @GetMapping("/listarCategorias")
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", repository.findAll(Sort.by("registroId")));
        return "Livros/Categorias/listaCategorias";
    }

    // O ID do banco é estável mesmo depois de excluir registros anteriores.
    @GetMapping("/editar/{registroId}")
    public String editarCategoria(@PathVariable Long registroId, Model model) {
        model.addAttribute("categoria", buscarCategoria(registroId));
        model.addAttribute("registroId", registroId);
        return "Livros/Categorias/categoriaFormulario";
    }

    @Transactional
    @PostMapping("/atualizar/{registroId}")
    public String atualizarCategoria(@PathVariable Long registroId,
                                     @ModelAttribute("categoria") Categoria categoria,
                                     BindingResult resultado, Model model) {
        Categoria existente = buscarCategoria(registroId);
        validar(categoria, resultado);
        if (resultado.hasErrors()) {
            // Preserva a URL de atualização ao mostrar os erros do formulário.
            model.addAttribute("registroId", registroId);
            return "Livros/Categorias/categoriaFormulario";
        }
        // Grava os campos editáveis na entidade carregada, mantendo a chave do banco.
        existente.setId(categoria.getId());
        existente.setNome(categoria.getNome());
        repository.save(existente);
        return "redirect:/livros/categorias/listarCategorias";
    }

    // O link da tabela usa GET após a confirmação no navegador, como na aula.
    @Transactional
    @GetMapping("/excluir/{registroId}")
    public String excluirCategoria(@PathVariable Long registroId) {
        buscarCategoria(registroId);
        repository.deleteById(registroId);
        return "redirect:/livros/categorias/listarCategorias";
    }

    private Categoria buscarCategoria(Long registroId) {
        return repository.findById(registroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada."));
    }

    private void validar(Categoria categoria, BindingResult resultado) {
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "nome", "obrigatorio", "Informe o nome.");
        // A regra do valor mínimo só é aplicada se o campo estiver sem erro de conversão.
        if (!resultado.hasFieldErrors("id") && categoria.getId() < 1) {
            resultado.rejectValue("id", "invalido", "Informe um ID maior que zero.");
        }
    }
}
