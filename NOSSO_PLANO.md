# NOSSO PLANO DE AÇÃO: APP NETFLIX API

Este documento descreve o plano de desenvolvimento para implementar as funcionalidades de usuário no aplicativo, utilizando a API do The Movie Database (TMDB).

### Princípio Fundamental
O TMDB é a fonte única da verdade para todos os dados de filmes (títulos, pôsteres, descrições, etc.). Nosso aplicativo atua como um cliente que gerencia **listas de referências (IDs)** a esses filmes. **Nós nunca criamos, editamos ou armazenamos os detalhes dos filmes**, apenas os buscamos da API para exibição.

---

### **Prioridade 1: Autenticação do Usuário (Obter `session_id`)**
**Objetivo:** Permitir que o usuário autorize nosso aplicativo a realizar ações em sua conta TMDB. Esta é a base para todas as funcionalidades personalizadas.

**Plano de Ação:**
1.  **Ponto de Entrada:**
    *   No menu da `MainActivity`, no `onClick` de "Minha Lista", verificar se uma `session_id` está salva localmente (`DataStore` para o Compose).
    *   Se **sim**, navegar para `MyListScreen`.
    *   Se **não**, navegar para uma nova tela: `LoginScreen.kt`.

2.  **Tela de Login (`LoginScreen.kt`):**
    *   Criar uma nova tela com uma UI simples: um logo e um botão "Entrar com TMDB".

3.  **ViewModel de Login (`LoginViewModel.kt`):**
    *   Esta será a central de toda a lógica de autenticação.
    *   Ao clicar no botão "Entrar com TMDB":
        a. Fazer a chamada `GET` para `/authentication/token/new` para obter um `request_token`.
        b. Abrir uma **WebView** ou **Chrome Custom Tab** apontando para `https://www.themoviedb.org/authenticate/{REQUEST_TOKEN_OBTIDO}`.
        c. Monitorar a URL da WebView para detectar o redirecionamento de volta ao app, que indica que o usuário completou a autorização.
        d. Com o `request_token` agora aprovado, fazer a chamada `POST` para `/authentication/session/new` para obter a `session_id` final.

4.  **Armazenamento Seguro:**
    *   Salvar a `session_id` recebida de forma segura no dispositivo usando **Jetpack DataStore** (preferencial).

**Endpoints a serem usados:**
*   `GET /authentication/token/new`
*   `POST /authentication/session/new`

---

### **Prioridade 2: Gerenciamento da "Minha Lista" (Lógica Unificada)**
**Objetivo:** Centralizar toda a lógica de interação com a "Minha Lista" na tela `MyMovieDetails`, que se tornará a tela de detalhes padrão para qualquer filme no aplicativo.

**Plano de Ação:**
1.  **Criação do `MyMovieDetailsViewModel.kt`:**
    *   Este ViewModel será o cérebro da tela. Ao ser inicializado, ele receberá o `movie_id` do filme.
    *   Sua primeira tarefa é verificar (usando a `session_id` e o `list_id` da lista principal) se este `movie_id` **já existe** na "Minha Lista" do usuário. O resultado dessa verificação controlará o estado do botão no `SplitButtonLayout`.

2.  **Lógica do `SplitButtonLayout` em `MyMovieDetails.kt`:**
    *   **Cenário 1: Filme NÃO está na lista.**
        *   O botão principal exibirá "Adicionar à Lista".
        *   O `onClick` acionará a função de **adicionar** no ViewModel, que deve:
            a. Verificar se a `session_id` existe (se não, redirecionar para o login).
            b. Garantir que a lista principal "Minha Lista" exista e que temos o `list_id` (criando-a na primeira vez, se necessário, com `POST /list`).
            c. Executar a chamada `POST /list/{list_id}/add_item`, passando o `movie_id`.
            d. Após o sucesso, atualizar a UI para refletir que o filme foi adicionado (o botão agora deve mudar para o cenário 2).
    *   **Cenário 2: Filme JÁ ESTÁ na lista.**
        *   O botão principal exibirá "Remover da Lista" ou um ícone de `✓`.
        *   O `onClick` acionará a função de **remover** no ViewModel.
        *   A função deve chamar `POST /list/{list_id}/remove_item`, passando o `movie_id`.
        *   Após o sucesso, atualizar a UI para refletir que o filme foi removido (o botão agora deve voltar para o cenário 1).

**Endpoints a serem usados:**
*   `POST /list` (para criar a lista na primeira vez)
*   `POST /list/{list_id}/add_item`
*   `POST /list/{list_id}/remove_item`
*   `GET /list/{list_id}` (para verificar o conteúdo da lista e determinar o estado inicial do botão)
*   `GET /account/{account_id}/lists` (para buscar o `list_id` se ele for perdido)

---

### **Prioridade 3: Exibição da "Minha Lista"**
**Objetivo:** Mostrar ao usuário os filmes que ele adicionou à sua lista.

**Plano de Ação:**
1.  **ViewModel (`MyListViewModel.kt`):**
    *   Quando a `MyListScreen` for iniciada, o `ViewModel` deve usar a `session_id` e o `list_id` salvos.
    *   Fazer a chamada `GET /list/{list_id}` para buscar todos os filmes contidos na lista.

2.  **Tela (`MyListScreen.kt`):**
    *   A tela deve observar o estado do `ViewModel`.
    *   Exibir uma `LazyColumn` ou `LazyVerticalGrid` com os filmes retornados pela API.
    *   Mostrar um estado de "Carregando..." enquanto a chamada está em progresso e uma mensagem de "Sua lista está vazia" se a API não retornar filmes.

3.  **Navegação:**
    *   O clique em cada item da grade deve navegar para `MyMovieDetails.kt`, passando o `movie_id` correspondente.

**Endpoints a serem usados:**
*   `GET /list/{list_id}`

---

### **Prioridade 4: Melhorias e Funcionalidades Futuras**
**Objetivo:** Refinar a experiência do usuário e adicionar funcionalidades secundárias.

*   **Feedback Visual:** Adicionar indicadores de carregamento (`CircularProgressIndicator`) em todas as telas durante as chamadas de API e usar `Snackbar` para mensagens de sucesso/erro (ex: "Filme adicionado à lista!").
*   **Gerenciamento de Múltiplas Listas:** Transformar a `MovieForm.kt` em `ListForm.kt` para permitir que usuários avançados criem e gerenciem múltiplas listas personalizadas.
*   **Estado Offline:** Implementar um banco de dados local (Room) para fazer cache da "Minha Lista", permitindo que o usuário veja seus filmes salvos mesmo sem conexão à internet.
