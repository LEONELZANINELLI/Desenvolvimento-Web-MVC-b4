
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

/** Coordena o CRUD de livros e devolve as views usadas pelo Thymeleaf. */
@Controller
@RequestMapping("/livros")
public class LivroController {

    // A lista vive no controller e é recriada vazia ao reiniciar a aplicação.
    private final List<Livro> livros = new ArrayList<>();

    // A chave do Model é lida como ${livros} pelo template da listagem.
    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("livros", livros);
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
        livros.add(livro);
        // Post/Redirect/Get faz o navegador abrir a confirmação em uma nova requisição GET.
        return "redirect:/livros/confirmacao";
    }

    // O índice vem da posição da linha na tabela. Sua presença ativa o modo de edição.
    @GetMapping("/editar/{indice}")
    public String editar(@PathVariable int indice, Model model) {
        verificarIndice(indice);
        model.addAttribute("livro", livros.get(indice));
        model.addAttribute("indice", indice);
        return "Livros/formulario";
    }

    // A edição substitui o item na posição existente e mantém o tamanho da lista.
    @PostMapping("/atualizar/{indice}")
    public String atualizar(@PathVariable int indice,
                            @ModelAttribute("livro") Livro livro,
                            BindingResult resultado, Model model) {
        verificarIndice(indice);
        validar(resultado);
        if (resultado.hasErrors()) {
            // Preserva a URL de atualização ao exibir novamente um formulário inválido.
            model.addAttribute("indice", indice);
            return "Livros/formulario";
        }
        livros.set(indice, livro);
        return "redirect:/livros";
    }

    // O link da tabela usa GET após a confirmação do usuário no navegador.
    @GetMapping("excluir/{indice}")
    public String excluir(@PathVariable int indice) {
        verificarIndice(indice);
        livros.remove(indice);
        return "redirect:/livros";
    }

    // Converte uma posição fora dos limites em HTTP 404 antes de acessar a lista.
    private void verificarIndice(int indice) {
        if (indice < 0 || indice >= livros.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado.");
        }
    }

    // A validação no servidor também rejeita campos preenchidos apenas com espaços.
    private void validar(BindingResult resultado) {
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "titulo", "obrigatorio", "Informe o título.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "autor", "obrigatorio", "Informe o autor.");
        ValidationUtils.rejectIfEmptyOrWhitespace(resultado, "isbn", "obrigatorio", "Informe o ISBN.");
    }

}


