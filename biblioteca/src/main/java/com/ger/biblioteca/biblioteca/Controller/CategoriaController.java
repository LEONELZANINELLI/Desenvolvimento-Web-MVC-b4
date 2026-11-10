
package com.ger.biblioteca.biblioteca.Controller;

import com.ger.biblioteca.biblioteca.model.Categoria;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            Model model) {

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

    // GET - Excluir categoria pelo indice
    @GetMapping("/excluir/{indice}")
    public String excluirCategoria(@PathVariable int indice) {

        if (indice >= 0 && indice < categorias.size()) {
            categorias.remove(indice);
        }

        return "redirect:/livros/categorias/listarCategorias";
    }
}
