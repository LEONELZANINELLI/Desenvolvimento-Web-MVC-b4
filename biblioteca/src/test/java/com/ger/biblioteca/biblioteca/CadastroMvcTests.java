package com.ger.biblioteca.biblioteca;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
// Recria o contexto para que cada cenário use um banco H2 de teste vazio.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CadastroMvcTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

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
        prepararAutoresDosLivros(original, atualizado);
        mvc.perform(get(formulario)).andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"" + base + "/salvar\"")));

        cadastrar(base, original);
        cadastrar(base, atualizado);
        List<Long> ids = idsRegistrados(atributo);
        assertThat(ids).hasSize(2);
        String primeiroId = ids.get(0).toString();
        String segundoId = ids.get(1).toString();
        mvc.perform(get(listagem)).andExpect(status().isOk())
                .andExpect(model().attribute(atributo, hasSize(2)))
                .andExpect(content().string(containsString("<tbody>")))
                .andExpect(content().string(containsString("href=\"" + base + "/editar/" + segundoId + "\"")))
                .andExpect(content().string(containsString("href=\"" + base + "/excluir/" + segundoId + "\"")));

        ResultActions edicao = mvc.perform(get(base + "/editar/" + segundoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"" + base + "/atualizar/" + segundoId + "\"")))
                .andExpect(content().string(containsString(">Salvar alterações</button>")));
        for (Map.Entry<String, String> campo : atualizado.entrySet()) {
            if (campo.getKey().equals("autor")) {
                edicao.andExpect(content().string(containsString(
                        "value=\"" + buscarIdAutor(campo.getValue()) + "\" selected=\"selected\">" + campo.getValue())));
            } else {
                edicao.andExpect(content().string(containsString("value=\"" + campo.getValue() + "\"")));
            }
        }

        mvc.perform(post(base + "/atualizar/" + segundoId).params(parametros(original)))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(listagem));
        assertThat(idsRegistrados(atributo)).containsExactlyElementsOf(ids);
        ResultActions lista = mvc.perform(get(listagem)).andExpect(status().isOk())
                .andExpect(model().attribute(atributo, hasSize(2)))
                .andExpect(content().string(containsString("<tbody>")));
        for (String valor : original.values()) {
            lista.andExpect(resultado -> assertThat(org.springframework.util.StringUtils.countOccurrencesOf(
                    resultado.getResponse().getContentAsString(), "<td>" + valor + "</td>")).isEqualTo(2));
        }

        mvc.perform(get(base + "/excluir/" + primeiroId)).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(listagem));
        assertThat(idsRegistrados(atributo)).containsExactly(ids.get(1));
        mvc.perform(get(listagem)).andExpect(status().isOk())
                .andExpect(model().attribute(atributo, hasSize(1)))
                .andExpect(content().string(containsString("href=\"" + base + "/editar/" + segundoId + "\"")));
        mvc.perform(get(base + "/editar/" + primeiroId)).andExpect(status().isNotFound());
        mvc.perform(get(base + "/excluir/" + segundoId)).andExpect(status().is3xxRedirection());
        assertThat(idsRegistrados(atributo)).isEmpty();
        mvc.perform(get(listagem)).andExpect(status().isOk())
                .andExpect(model().attribute(atributo, hasSize(0)))
                .andExpect(content().string(not(containsString("<tbody>"))));
    }

    @ParameterizedTest(name = "Dados inválidos não mudam os registros de {0}")
    @MethodSource("cadastros")
    void deveValidarCadastroEAtualizacaoSemPerderOsDados(String base, String formulario,
            String listagem, String atributo, Map<String, String> original,
            Map<String, String> atualizado) throws Exception {
        prepararAutoresDosLivros(original, atualizado);
        MultiValueMap<String, String> vazio = parametros(original);
        vazio.replaceAll((campo, valores) -> List.of("   "));

        mvc.perform(post(base + "/salvar").params(vazio)).andExpect(status().isOk())
                .andExpect(model().hasErrors()).andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(containsString("action=\"" + base + "/salvar\"")));
        mvc.perform(get(listagem)).andExpect(model().attribute(atributo, hasSize(0)));

        cadastrar(base, original);
        String atualizacao = base + "/atualizar/" + idsRegistrados(atributo).get(0);
        ResultActions erro = mvc.perform(post(atualizacao).params(vazio))
                .andExpect(status().isOk()).andExpect(model().hasErrors())
                .andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(containsString("action=\"" + atualizacao + "\"")));
        erro.andExpect(content().string(containsString("value=\"   \"")));
        ResultActions lista = mvc.perform(get(listagem))
                .andExpect(model().attribute(atributo, hasSize(1)));
        for (String valor : original.values()) {
            lista.andExpect(content().string(containsString("<td>" + valor + "</td>")));
        }
        mvc.perform(post(atualizacao).params(parametros(atualizado)))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(listagem));
    }

    @ParameterizedTest
    @CsvSource({"/livros, -1", "/livros, 0", "/livros, 5", "/autor, -1", "/autor, 0", "/autor, 5",
            "/livros/categorias, -1", "/livros/categorias, 0", "/livros/categorias, 5"})
    void deveRetornar404ParaRegistroInexistente(String base, long id) throws Exception {
        mvc.perform(get(base + "/editar/" + id)).andExpect(status().isNotFound());
        mvc.perform(post(base + "/atualizar/" + id)).andExpect(status().isNotFound());
        mvc.perform(get(base + "/excluir/" + id)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({"0", "-1", "abc"})
    void deveRecusarIdDeCategoriaInvalido(String id) throws Exception {
        mvc.perform(post("/livros/categorias/salvar").param("id", id).param("nome", "Romance"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("categoria", "id"));
        mvc.perform(get("/livros/categorias/listarCategorias"))
                .andExpect(model().attribute("categorias", hasSize(0)));
    }

    @Test
    void deveVincularVariosLivrosAoMesmoAutorERefletirORenomeio() throws Exception {
        String nomeAutor = "Machado de Assis";
        cadastrarAutor(nomeAutor);
        Long autorId = buscarIdAutor(nomeAutor);
        cadastrar("/livros", Map.of("titulo", "Dom Casmurro", "autor", nomeAutor, "isbn", "111"));
        cadastrar("/livros", Map.of("titulo", "O Alienista", "autor", nomeAutor, "isbn", "222"));
        assertThat(jdbc.queryForList("SELECT autor_id FROM livro ORDER BY id", Long.class))
                .containsExactly(autorId, autorId);

        mvc.perform(post("/autor/atualizar/" + autorId)
                        .param("nome", "Machado de Assis atualizado")
                        .param("descricao", "Escritor").param("nacionalidade", "Brasileira"))
                .andExpect(redirectedUrl("/autor"));
        mvc.perform(get("/livros")).andExpect(content().string(containsString("Machado de Assis atualizado")))
                .andExpect(content().string(not(containsString("<td>Machado de Assis</td>"))));

        var exclusao = mvc.perform(get("/autor/excluir/" + autorId))
                .andExpect(redirectedUrl("/autor"))
                .andExpect(flash().attributeExists("erroExclusao")).andReturn();
        mvc.perform(get("/autor").flashAttrs(exclusao.getFlashMap()))
                .andExpect(content().string(containsString("Este autor está vinculado a um livro.")));
        assertThat(idsRegistrados("autores")).containsExactly(autorId);
        assertThat(idsRegistrados("livros")).hasSize(2);
    }

    @Test
    void deveExigirUmAutorCadastradoParaLivroNovo() throws Exception {
        mvc.perform(get("/livros/novo"))
                .andExpect(content().string(containsString("Cadastre um autor para continuar.")))
                .andExpect(content().string(containsString("href=\"/autor/novo\"")))
                .andExpect(content().string(containsString("disabled=\"disabled\"")));

        for (String autorId : List.of("", "99")) {
            mvc.perform(post("/livros/salvar")
                            .param("titulo", "O Alienista").param("isbn", "222").param("autorId", autorId))
                    .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("livro", "autorId"));
        }
        assertThat(idsRegistrados("livros")).isEmpty();
    }

    @Test
    void devePreservarNomeDeAutorEmLivroAntigoEAceitarVinculoPosterior() throws Exception {
        jdbc.update("INSERT INTO livro (titulo, autor, isbn) VALUES (?, ?, ?)",
                "Livro antigo", "Autor informado antes do vínculo", "111");
        Long livroId = idsRegistrados("livros").get(0);
        String atualizar = "/livros/atualizar/" + livroId;

        mvc.perform(get("/livros/editar/" + livroId)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Manter autor atual")))
                .andExpect(content().string(containsString("Autor informado antes do vínculo")));
        mvc.perform(post(atualizar).param("titulo", "Título ajustado").param("isbn", "111").param("autorId", ""))
                .andExpect(redirectedUrl("/livros"));
        mvc.perform(get("/livros")).andExpect(content().string(containsString("Título ajustado")))
                .andExpect(content().string(containsString("Autor informado antes do vínculo")));

        cadastrarAutor("Autor cadastrado");
        Long autorId = buscarIdAutor("Autor cadastrado");
        mvc.perform(post(atualizar).param("titulo", "Título ajustado").param("isbn", "111")
                        .param("autorId", autorId.toString()))
                .andExpect(redirectedUrl("/livros"));
        assertThat(jdbc.queryForObject("SELECT autor_id FROM livro WHERE id = ?", Long.class, livroId))
                .isEqualTo(autorId);
        mvc.perform(get("/livros")).andExpect(content().string(containsString("<td>Autor cadastrado</td>")));
    }

    private void prepararAutoresDosLivros(Map<String, String> original, Map<String, String> atualizado)
            throws Exception {
        if (original.containsKey("autor")) {
            cadastrarAutor(original.get("autor"));
            cadastrarAutor(atualizado.get("autor"));
        }
    }

    private void cadastrarAutor(String nome) throws Exception {
        cadastrar("/autor", Map.of("nome", nome, "descricao", "Escritor", "nacionalidade", "Brasileira"));
    }

    private Long buscarIdAutor(String nome) {
        return jdbc.queryForObject("SELECT id FROM autor WHERE nome = ?", Long.class, nome);
    }

    private List<Long> idsRegistrados(String atributo) {
        String sql = switch (atributo) {
            case "livros" -> "SELECT id FROM livro ORDER BY id";
            case "autores" -> "SELECT id FROM autor ORDER BY id";
            case "categorias" -> "SELECT registro_id FROM categoria ORDER BY registro_id";
            default -> throw new IllegalArgumentException("Cadastro desconhecido: " + atributo);
        };
        return jdbc.queryForList(sql, Long.class);
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
        dados.forEach((campo, valor) -> {
            if (campo.equals("autor")) {
                parametros.add("autorId", buscarIdAutor(valor).toString());
            } else {
                parametros.add(campo, valor);
            }
        });
        return parametros;
    }
}
