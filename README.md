# Desenvolvimento-Web-MVC-b4
Projetos e atividades das aulas de Desenvolvimento Web com MVC

## Biblioteca

Aplicação em Java com Spring MVC, Thymeleaf e Spring Data JPA para gerenciar livros, autores e categorias em um banco H2.

| Entidade | Cadastro | Listagem | Edição | Atualização | Exclusão |
| --- | --- | --- | --- | --- | --- |
| Livro | Sim | Sim | Sim | Sim | Sim |
| Autor | Sim | Sim | Sim | Sim | Sim |
| Categoria | Sim | Sim | Sim | Sim | Sim |

O botão **Editar** abre o formulário preenchido. **Salvar alterações** atualiza o registro no banco pela sua chave primária. A exclusão pede confirmação no navegador. Os campos obrigatórios também são validados no servidor.

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

Abra <http://localhost:8088>. A página inicial permite acessar os livros, os autores e as categorias.

O H2 grava os dados em `data/biblioteca.mv.db`, que permanece após reiniciar a aplicação. O caminho é relativo à pasta de execução. No IntelliJ, use como diretório de trabalho a pasta `biblioteca` que contém o `pom.xml`.

### Acessar o H2 Console

Com a aplicação rodando, abra <http://localhost:8088/h2-console> e preencha os campos abaixo.

| Campo | Valor |
| --- | --- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:file:./data/biblioteca` |
| User Name | `sa` |
| Password | Deixe em branco |

Clique em **Connect**. Cadastre um livro pela aplicação e rode estas consultas no console para ver o conteúdo das tabelas.

```sql
SELECT * FROM LIVRO;
SELECT * FROM AUTOR;
SELECT * FROM CATEGORIA;
```

O Hibernate cria ou atualiza o esquema ao iniciar. O H2 Console fica habilitado para acesso local durante a aula.

### Onde está cada parte do MVC

| Arquivo ou pasta | O que procurar |
| --- | --- |
| [`BibliotecaApplication.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/BibliotecaApplication.java) | `main()` inicia o Spring Boot. `@SpringBootApplication` habilita a configuração automática e a descoberta dos controllers nos subpacotes. |
| [`model`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/model) | `Livro`, `Autor` e `Categoria` são entidades JPA, cada uma com sua tabela e chave primária gerada pelo banco. O autor do livro continua sendo texto livre. |
| [`LivroController.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/Controller/LivroController.java) | CRUD de livros. Comece por `inicio()`, `editar()` e `atualizar()` para seguir uma requisição da lista ao formulário e de volta. |
| [`AutorController.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/Controller/AutorController.java) | CRUD de autores e página de confirmação do cadastro. |
| [`CategoriaController.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/Controller/CategoriaController.java) | CRUD de categorias, validação do ID e confirmação exibida após o cadastro. |
| [`ConfirmacaoController.java`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/Controller/ConfirmacaoController.java) | Redireciona `/` e `/Livros` para `/livros` e exibe a confirmação do cadastro de livro. |
| [`templates/Livros`](biblioteca/src/main/resources/templates/Livros) | Views HTML do Thymeleaf. Livros usam `inicio.html`, `formulario.html` e `confirmacao.html`. Autores e categorias têm suas versões nas subpastas `Autores` e `Categorias`. |
| [`repository`](biblioteca/src/main/java/com/ger/biblioteca/biblioteca/repository) | O Spring Data fornece a implementação de `JpaRepository` para consultar, salvar e excluir entidades. |
| [`application.properties`](biblioteca/src/main/resources/application.properties) e [`pom.xml`](biblioteca/pom.xml) | A configuração reúne a porta `8088`, a conexão com o H2 e o console. O POM declara Spring MVC, Thymeleaf, JPA e H2. |

Os modelos usam campos `private`, getters e setters. O GET de cadastro cria um objeto vazio. Na hora de renderizar, o Thymeleaf lê as propriedades desse objeto. No POST, o Spring converte os dados do formulário e preenche as propriedades do objeto recebido pelo controller.

### Rotas do CRUD

| Operação | Livro | Autor | Categoria |
| --- | --- | --- | --- |
| Listar | `GET /livros` | `GET /autor` | `GET /livros/categorias/listarCategorias` |
| Abrir cadastro | `GET /livros/novo` | `GET /autor/novo` | `GET /livros/categorias/categoriaFormulario` |
| Cadastrar | `POST /livros/salvar` | `POST /autor/salvar` | `POST /livros/categorias/salvar` |
| Abrir edição | `GET /livros/editar/{registroId}` | `GET /autor/editar/{registroId}` | `GET /livros/categorias/editar/{registroId}` |
| Atualizar | `POST /livros/atualizar/{registroId}` | `POST /autor/atualizar/{registroId}` | `POST /livros/categorias/atualizar/{registroId}` |
| Excluir | `GET /livros/excluir/{registroId}` | `GET /autor/excluir/{registroId}` | `GET /livros/categorias/excluir/{registroId}` |

### Como seguir a edição de um livro no código

1. `GET /livros` chama `inicio()`. O controller consulta `repository.findAll(...)` e entrega a lista à view pelo `Model`. Em `inicio.html`, `th:each` cria uma linha por livro, `th:text` exibe seus campos e `livro.id` forma os links de ação.
2. `GET /livros/editar/5` chama `editar()`. `@PathVariable` lê o ID da URL. `repository.findById(5L)` retorna um `Optional<Livro>`. O controller entrega o livro e o ID à view ou responde HTTP `404` se o registro estiver ausente.
3. Em `formulario.html`, `th:object` seleciona o livro e `th:field` associa os campos HTML às propriedades. Se o controller forneceu `registroId`, `th:action` envia o formulário para `/atualizar/{registroId}`. Um cadastro novo vai para `/salvar`.
4. O Spring preenche o objeto `@ModelAttribute` a partir do POST. O `BindingResult` reúne erros de conversão e validação. Em caso de erro, a view mostra as mensagens e mantém a URL de atualização.
5. Uma atualização válida copia os campos do formulário para a entidade existente e chama `save()` com a mesma chave. No cadastro, a entidade chega sem chave e o H2 gera uma. A exclusão chama `deleteById()`. A listagem reflete essas operações mesmo depois de reiniciar.

### Detalhes para conferir durante a demonstração

- **Chave primária e ID de categoria.** A chave permanece a mesma depois que outro registro é excluído. Em `Categoria`, `REGISTRO_ID` é a chave gerada pelo banco. `ID` é o número editável informado pelo usuário, validado como inteiro positivo. Dois registros podem ter o mesmo número no campo `ID`.
- **Validação.** `required` e `min` ajudam no navegador. O controller também valida os campos e rejeita textos formados só por espaços. O ISBN é tratado como texto obrigatório. O código atual aceita qualquer ISBN preenchido.
- **Confirmação.** Livro e autor usam Post/Redirect/Get. Depois de salvar, o POST redireciona para um GET da confirmação. Categoria renderiza a confirmação no próprio POST, e atualizar a página pode reenviar o formulário.
- **Exclusão.** O `confirm()` do navegador permite cancelar o clique. Na versão das aulas, a exclusão usa GET, embora GET seja destinado a consultas.

### Testes

No Windows, execute `.\mvnw.cmd clean verify` dentro de `biblioteca`. No Linux ou macOS, use `bash mvnw clean verify`.

Os testes usam H2 em memória. Eles verificam cadastro, edição, atualização sem duplicação, exclusão, campos obrigatórios e registros inexistentes, incluindo o HTML renderizado pelo Thymeleaf.

[`CadastroMvcTests`](biblioteca/src/test/java/com/ger/biblioteca/biblioteca/CadastroMvcTests.java) usa MockMvc para conferir as respostas e `JdbcTemplate` para conferir as chaves gravadas no H2 nos três cadastros. As classes `BibliotecaApplicationTests` verificam a inicialização do contexto Spring.
