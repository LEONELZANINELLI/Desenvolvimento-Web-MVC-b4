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

### Testes

No Windows, execute `.\mvnw.cmd clean verify` dentro de `biblioteca`. No Linux ou macOS, use `bash mvnw clean verify`.

Os testes verificam cadastro, formulário de edição preenchido, atualização sem duplicação, exclusão, campos obrigatórios e registros inexistentes, incluindo a renderização dos templates Thymeleaf.
