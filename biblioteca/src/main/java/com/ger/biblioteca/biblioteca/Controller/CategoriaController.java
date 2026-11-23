
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Categoria;
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
@RequestMapping("/livros/categorias")
public class CategoriaController {

    private final List<Categoria> categorias = new ArrayList<>();

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
        categorias.add(categoria);

        System.out.println("Nome: " + categoria.getNome());
        System.out.println("ID: " + categoria.getId());

        model.addAttribute("categoria", categoria);

        return "Livros/Categorias/confirmacaoCategoria";
    }

    // GET - Listar todas as categorias
    @GetMapping("/listarCategorias")
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categorias);
        return "Livros/Categorias/listaCategorias";
    }

    // GET - Abre o formulario com a categoria selecionada
    @GetMapping("/editar/{indice}")
    public String editarCategoria(@PathVariable int indice, Model model) {
        verificarIndice(indice);
        model.addAttribute("categoria", categorias.get(indice));
        model.addAttribute("indice", indice);
        return "Livros/Categorias/categoriaFormulario";
    }

    // POST - Atualiza os dados sem criar outra categoria
    @PostMapping("/atualizar/{indice}")
    public String atualizarCategoria(@PathVariable int indice,
                                      @ModelAttribute("categoria") Categoria categoria,
                                      BindingResult resultado, Model model) {
        verificarIndice(indice);
        validar(categoria, resultado);
        if (resultado.hasErrors()) {
            model.addAttribute("indice", indice);
            return "Livros/Categorias/categoriaFormulario";
        }
        categorias.set(indice, categoria);
        return "redirect:/livros/categorias/listarCategorias";
    }

    // GET - Excluir categoria pelo indice
    @GetMapping("/excluir/{indice}")
    public String excluirCategoria(@PathVariable int indice) {

        verificarIndice(indice);
        categorias.remove(indice);

        return "redirect:/livros/categorias/listarCategorias";
    }

    private void verificarIndice(int indice) {
        if (indice < 0 || indice >= categorias.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada.");
        }
    }

    private void validar(Categoria categoria, BindingResult resultado) {
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "nome", "obrigatorio", "Informe o nome.");
        if (!resultado.hasFieldErrors("id") && categoria.getId() < 1) {
            resultado.rejectValue("id", "invalido", "Informe um ID maior que zero.");
        }
    }
}
