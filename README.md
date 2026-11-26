# Desenvolvimento-Web-MVC-b4
Projetos e atividades das aulas de Desenvolvimento Web com MVC

## Biblioteca

Aplicação em Java com Spring MVC, Thymeleaf e HTML para gerenciar livros, autores e categorias.

| Entidade | Cadastro | Listagem | Edição | Atualização | Exclusão |
| --- | --- | --- | --- | --- | --- |
| Livro | Sim | Sim | Sim | Sim | Sim |
| Autor | Sim | Sim | Sim | Sim | Sim |
| Categoria | Sim | Sim | Sim | Sim | Sim |

O botão **Editar** abre o formulário preenchido. **Salvar alterações** atualiza o registro na lista, sem criar outro. A exclusão pede confirmação no navegador. Os campos obrigatórios também são validados no servidor.

### Executar

Requer Java 17 ou superior. O Maven Wrapper já está incluído.

No PowerShell, na pasta do repositório:

```powershell
cd biblioteca
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
cd biblioteca
bash mvnw spring-boot:run
```

Abra <http://localhost:8081>. A página inicial permite acessar os livros, os autores e as categorias.

Os dados ficam em listas na memória, seguindo a implementação das aulas. Ao reiniciar a aplicação, os cadastros são apagados; não é necessário configurar banco de dados.

### Onde está cada parte do MVC

| Arquivo ou pasta | O que procurar |
| --- | --- |
| [`BibliotecaApplication.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/BibliotecaApplication.java) | `main()` inicia o Spring Boot. `@SpringBootApplication` habilita a configuração automática e a descoberta dos controllers nos subpacotes. |
| [`model`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/model) | `Livro` guarda título, autor em texto livre e ISBN. `Autor` guarda nome, descrição e nacionalidade. `Categoria` guarda ID e nome. São três cadastros independentes. |
| [`LivroController.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/Controller/LivroController.java) | CRUD de livros. Comece por `inicio()`, `editar()` e `atualizar()` para seguir uma requisição da lista ao formulário e de volta. |
| [`AutorController.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/Controller/AutorController.java) | CRUD de autores e página de confirmação do cadastro. |
| [`CategoriaController.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/Controller/CategoriaController.java) | CRUD de categorias, validação do ID e confirmação exibida após o cadastro. |
| [`ConfirmacaoController.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/Controller/ConfirmacaoController.java) | Redireciona `/` e `/Livros` para `/livros` e exibe a confirmação do cadastro de livro. |
| [`templates/Livros`](biblioteca/src/main/resources/templates/Livros) | Views HTML do Thymeleaf. Livros usam `inicio.html`, `formulario.html` e `confirmacao.html`. Autores e categorias têm suas versões nas subpastas `Autores` e `Categorias`. |
| [`application.properties`](biblioteca/src/main/resources/application.properties) e [`pom.xml`](biblioteca/pom.xml) | A configuração define a porta `8081`. O POM define a versão do Java, as dependências do Spring MVC e do Thymeleaf e o build Maven. |

Os modelos usam campos `private`, getters e setters. O GET de cadastro cria um objeto vazio. Na hora de renderizar, o Thymeleaf lê as propriedades desse objeto. No POST, o Spring converte os dados do formulário e preenche as propriedades do objeto recebido pelo controller.

### Rotas do CRUD

| Operação | Livro | Autor | Categoria |
| --- | --- | --- | --- |
| Listar | `GET /livros` | `GET /autor` | `GET /livros/categorias/listarCategorias` |
| Abrir cadastro | `GET /livros/novo` | `GET /autor/novo` | `GET /livros/categorias/categoriaFormulario` |
| Cadastrar | `POST /livros/salvar` | `POST /autor/salvar` | `POST /livros/categorias/salvar` |
| Abrir edição | `GET /livros/editar/{indice}` | `GET /autor/editar/{indice}` | `GET /livros/categorias/editar/{indice}` |
| Atualizar | `POST /livros/atualizar/{indice}` | `POST /autor/atualizar/{indice}` | `POST /livros/categorias/atualizar/{indice}` |
| Excluir | `GET /livros/excluir/{indice}` | `GET /autor/excluir/{indice}` | `GET /livros/categorias/excluir/{indice}` |

### Como seguir a edição de um livro no código

1. `GET /livros` chama `inicio()`. `model.addAttribute("livros", livros)` entrega a lista à view. Em `inicio.html`, `th:each` cria uma linha por livro, `th:text` exibe seus campos e `status.index` coloca a posição do item no `th:href` de edição.
2. `GET /livros/editar/0` chama `editar()`. `@PathVariable` lê o `0` da URL. O controller verifica os limites, usa `livros.get(0)` e entrega o livro e o índice ao `Model`. Uma posição fora da lista recebe HTTP `404`.
3. Em `formulario.html`, `th:object` seleciona o livro e `th:field` associa os campos HTML às propriedades. `th:action` escolhe a URL pelo índice. Com índice vai para `/atualizar/{indice}`. Sem índice vai para `/salvar`.
4. O Spring fornece o objeto preenchido ao parâmetro `@ModelAttribute` do método de POST. `BindingResult` vem logo depois e reúne erros de conversão e da validação feita no controller. Se houver erros, a view do formulário exibe as mensagens e o índice continua disponível para reenviar a edição.
5. Uma atualização válida usa `set(indice, objeto)` e redireciona para a lista. O cadastro usa `add(objeto)` para incluir mais um item. A exclusão usa `remove(indice)`.

### Detalhes para conferir durante a demonstração

- **Posição e ID.** Os links dos três cadastros usam a posição na lista, que começa em zero e pode mudar após uma exclusão. O `id` de `Categoria` vem do formulário. O código exige um inteiro positivo, mas aceita IDs repetidos.
- **Validação.** `required` e `min` ajudam no navegador. O controller também valida os campos e rejeita textos formados só por espaços. O ISBN é tratado como texto obrigatório. O código atual aceita qualquer ISBN preenchido.
- **Confirmação.** Livro e autor usam Post/Redirect/Get. Depois de salvar, o POST redireciona para um GET da confirmação. Categoria renderiza a confirmação no próprio POST, e atualizar a página pode reenviar o formulário.
- **Exclusão.** O `confirm()` do navegador permite cancelar o clique. Na versão das aulas, a exclusão usa GET, embora GET seja destinado a consultas.

### Testes

No Windows, execute `.\mvnw.cmd clean verify` dentro de `biblioteca`. No Linux ou macOS, use `bash mvnw clean verify`.

Os testes verificam cadastro, formulário de edição preenchido, atualização sem duplicação, exclusão, campos obrigatórios e registros inexistentes, incluindo a renderização dos templates Thymeleaf.

[`CadastroMvcTests`](biblioteca/src/test/java/com/ger/biblioteca/biblioteca/CadastroMvcTests.java) usa MockMvc para conferir as respostas e parametriza os mesmos cenários para livro, autor e categoria. As classes `BibliotecaApplicationTests` verificam a inicialização do contexto Spring.
