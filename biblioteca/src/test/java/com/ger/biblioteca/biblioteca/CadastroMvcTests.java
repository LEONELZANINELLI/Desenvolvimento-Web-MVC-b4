package com.ger.biblioteca.biblioteca;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exercita os três cadastros com requisições simuladas e verifica o HTML renderizado. */
@SpringBootTest
@AutoConfigureMockMvc
// Recria o contexto para isolar as listas em memória entre os cenários.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CadastroMvcTests {

    @Autowired
    private MockMvc mvc;

    static Stream<Arguments> cadastros() {
        return Stream.of(
                Arguments.of("/livros", "/livros/novo", "/livros", "livros",
                        Map.of("titulo", "Dom Casmurro", "autor", "Machado de Assis", "isbn", "9788535914849"),
                        Map.of("titulo", "O Alienista", "autor", "Machado", "isbn", "9788535914856")),
                Arguments.of("/autor", "/autor/novo", "/autor", "autores",
                        Map.of("nome", "Machado de Assis", "descricao", "Escritor", "nacionalidade", "Brasileira"),
                        Map.of("nome", "Clarice Lispector", "descricao", "Escritora", "nacionalidade", "Brasileira naturalizada")),
                Arguments.of("/livros/categorias", "/livros/categorias/categoriaFormulario",
                        "/livros/categorias/listarCategorias", "categorias",
                        Map.of("id", "1", "nome", "Romance"),
                        Map.of("id", "2", "nome", "Literatura brasileira"))
        );
    }

    @ParameterizedTest(name = "Cadastro, edição, atualização e exclusão em {0}")
    @MethodSource("cadastros")
    void deveCompletarOCicloSemDuplicarOuAlterarOutroRegistro(String base, String formulario,
            String listagem, String atributo, Map<String, String> original,
            Map<String, String> atualizado) throws Exception {
        mvc.perform(get(formulario)).andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"" + base + "/salvar\"")));

        cadastrar(base, original);
        cadastrar(base, atualizado);
        mvc.perform(get(listagem)).andExpect(status().isOk())
                .andExpect(model().attribute(atributo, hasSize(2)))
                .andExpect(content().string(containsString("<tbody>")))
                .andExpect(content().string(containsString("href=\"" + base + "/editar/1\"")))
                .andExpect(content().string(containsString("href=\"" + base + "/excluir/1\"")));

        ResultActions edicao = mvc.perform(get(base + "/editar/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"" + base + "/atualizar/1\"")))
                .andExpect(content().string(containsString(">Salvar alterações</button>")));
        for (Map.Entry<String, String> campo : atualizado.entrySet()) {
            edicao.andExpect(content().string(containsString("value=\"" + campo.getValue() + "\"")));
        }

        mvc.perform(post(base + "/atualizar/1").params(parametros(original)))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(listagem));
        ResultActions lista = mvc.perform(get(listagem)).andExpect(status().isOk())
                .andExpect(model().attribute(atributo, hasSize(2)))
                .andExpect(content().string(containsString("<tbody>")));
        for (String valor : original.values()) {
            lista.andExpect(resultado -> assertThat(org.springframework.util.StringUtils.countOccurrencesOf(
                    resultado.getResponse().getContentAsString(), "<td>" + valor + "</td>")).isEqualTo(2));
        }

        mvc.perform(get(base + "/excluir/0")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(listagem));
        mvc.perform(get(listagem)).andExpect(status().isOk())
                .andExpect(model().attribute(atributo, hasSize(1)))
                .andExpect(content().string(containsString("href=\"" + base + "/editar/0\"")));
        mvc.perform(get(base + "/excluir/0")).andExpect(status().is3xxRedirection());
        mvc.perform(get(listagem)).andExpect(status().isOk())
                .andExpect(model().attribute(atributo, hasSize(0)))
                .andExpect(content().string(not(containsString("<tbody>"))));
    }

    @ParameterizedTest(name = "Dados inválidos não mudam os registros de {0}")
    @MethodSource("cadastros")
    void deveValidarCadastroEAtualizacaoSemPerderOsDados(String base, String formulario,
            String listagem, String atributo, Map<String, String> original,
            Map<String, String> atualizado) throws Exception {
        MultiValueMap<String, String> vazio = parametros(original);
        original.keySet().forEach(campo -> vazio.set(campo, "   "));

        mvc.perform(post(base + "/salvar").params(vazio)).andExpect(status().isOk())
                .andExpect(model().hasErrors()).andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(containsString("action=\"" + base + "/salvar\"")));
        mvc.perform(get(listagem)).andExpect(model().attribute(atributo, hasSize(0)));

        cadastrar(base, original);
        ResultActions erro = mvc.perform(post(base + "/atualizar/0").params(vazio))
                .andExpect(status().isOk()).andExpect(model().hasErrors())
                .andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(containsString("action=\"" + base + "/atualizar/0\"")));
        erro.andExpect(content().string(containsString("value=\"   \"")));
        ResultActions lista = mvc.perform(get(listagem))
                .andExpect(model().attribute(atributo, hasSize(1)));
        for (String valor : original.values()) {
            lista.andExpect(content().string(containsString("<td>" + valor + "</td>")));
        }
        mvc.perform(post(base + "/atualizar/0").params(parametros(atualizado)))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(listagem));
    }

    @ParameterizedTest
    @CsvSource({"/livros, -1", "/livros, 0", "/livros, 5", "/autor, -1", "/autor, 0", "/autor, 5",
            "/livros/categorias, -1", "/livros/categorias, 0", "/livros/categorias, 5"})
    void deveRetornar404ParaRegistroInexistente(String base, int indice) throws Exception {
        mvc.perform(get(base + "/editar/" + indice)).andExpect(status().isNotFound());
        mvc.perform(post(base + "/atualizar/" + indice)).andExpect(status().isNotFound());
        mvc.perform(get(base + "/excluir/" + indice)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({"0", "-1", "abc"})
    void deveRecusarIdDeCategoriaInvalido(String id) throws Exception {
        mvc.perform(post("/livros/categorias/salvar").param("id", id).param("nome", "Romance"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("categoria", "id"));
        mvc.perform(get("/livros/categorias/listarCategorias"))
                .andExpect(model().attribute("categorias", hasSize(0)));
    }

    private void cadastrar(String base, Map<String, String> dados) throws Exception {
        ResultActions resultado = mvc.perform(post(base + "/salvar").params(parametros(dados)));
        if (base.equals("/livros/categorias")) {
            resultado.andExpect(status().isOk()).andExpect(view().name("Livros/Categorias/confirmacaoCategoria"));
        } else {
            resultado.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(base + "/confirmacao"));
            mvc.perform(get(base + "/confirmacao")).andExpect(status().isOk());
        }
    }

    private MultiValueMap<String, String> parametros(Map<String, String> dados) {
        MultiValueMap<String, String> parametros = new LinkedMultiValueMap<>();
        dados.forEach(parametros::add);
        return parametros;
    }
}
