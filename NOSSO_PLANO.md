# NOSSO PLANO DE AÇÃO: APP NETFLIX API

Este documento descreve o plano de desenvolvimento para implementar as funcionalidades de usuário no aplicativo, utilizando a API do The Movie Database (TMDB).

### Princípio Fundamental
O TMDB é a fonte única da verdade para todos os dados de filmes e séries. Nosso aplicativo atua como um cliente que gerencia **listas de referências (IDs)** a esses itens. **Nós nunca criamos, editamos ou armazenamos os detalhes dos itens**, apenas os buscamos da API para exibição. Devido a limitações e bugs na API v3, a funcionalidade de gerenciamento de listas será migrada para a API v4.

---

### **Prioridade 1: Autenticação de Usuário (API v4 - Obter `access_token`) - PENDENTE**
**Objetivo:** Permitir que o usuário autorize nosso aplicativo a realizar ações em sua conta TMDB usando o fluxo de autenticação moderno e seguro da API v4.

**Plano de Ação:**
1.  **Ponto de Entrada:**
    *   Verificar se um `access_token` da v4 está salvo localmente no `DataStore`.
    *   Se **sim**, o usuário está logado.
    *   Se **não**, navegar para `LoginScreen.kt`.

2.  **ViewModel de Login (`LoginViewModel.kt`):**
    *   Refatorar a lógica de autenticação para seguir o fluxo OAuth 2.0 da v4.
    *   Ao clicar no botão "Entrar com TMDB":
        a. Fazer a chamada `POST` para `/4/auth/request_token` para obter um `request_token` da v4.
        b. Abrir uma **Chrome Custom Tab** apontando para `https://www.themoviedb.org/auth/access?request_token={REQUEST_TOKEN_V4}`.
        c. Após a autorização do usuário, o TMDB redirecionará para o `redirect_to` configurado no site (nosso deep link do app).
        d. Com o `request_token` aprovado, fazer a chamada `POST` para `/4/auth/access_token` para obter o `access_token` e `account_id` finais.

3.  **Armazenamento Seguro:**
    *   Salvar o `access_token` e `account_id` recebidos de forma segura no `UserPreferencesRepository` (DataStore). O `session_id` da v3 será descontinuado.

**Endpoints a serem usados (API v4):**
*   `POST /4/auth/request_token`
*   `POST /4/auth/access_token`

---

### **Prioridade 2: Gerenciamento de Listas (API v4) - PENDENTE**
**Objetivo:** Utilizar a API v4, que é mais robusta e explícita, para adicionar e remover filmes E séries de listas de forma confiável, resolvendo o bug "Entry not found" da API v3.

**Plano de Ação:**
1.  **Atualizar `FilmeAPI.kt`:**
    *   Adicionar novos métodos para os endpoints da v4.
    *   Esses métodos exigirão um cabeçalho de autorização diferente: `Authorization: Bearer {ACCESS_TOKEN}`.

2.  **Refatorar `MyMovieDetailsViewModel` e `MySerieDetailsViewModel`:**
    *   Modificar as funções `addOrRemove...` para chamar os novos endpoints da v4.
    *   A requisição para **adicionar** um item agora enviará um corpo JSON especificando o `media_type` e o `media_id`, o que garante que o item correto seja adicionado. Ex: `{"items": [{"media_type": "tv", "media_id": 44217}]}`.
    *   A requisição para **remover** itens seguirá o mesmo padrão.

**Endpoints a serem usados (API v4):**
*   `POST /4/list/{list_id}/items` (para adicionar itens)
*   `DELETE /4/list/{list_id}/items` (para remover itens)
*   `GET /4/list/{list_id}` (para verificar o conteúdo da lista, agora com dados corretos e paginação)

---

### **Prioridade 3: Exibição da "Minha Lista" (Estratégia de Contorno) - CONCLUÍDA**
**Objetivo:** Mostrar ao usuário os itens que ele adicionou à sua lista, contornando um bug da API v3 que corrompe os dados de séries.

**Plano de Ação:**
1.  **ViewModel (`MyListViewModel.kt`) e PagingSource (`MyListPagingSource.kt`):**
    *   A `MyListPagingSource` implementa uma estratégia de **busca em duas etapas** para garantir a precisão dos dados.
    *   **Etapa A:** Faz a chamada `GET /list/{list_id}` (API v3) para obter a lista de IDs dos itens. Os detalhes retornados por esta chamada são ignorados por não serem confiáveis.
    *   **Etapa B:** Para cada ID obtido, uma nova chamada de API é feita ao seu endpoint canônico (`/movie/{id}` ou `/tv/{id}`) para buscar os detalhes corretos. Isso contorna o bug da API v3 e garante que "Vikings" seja exibido como "Vikings", e não como "Jasmine Women".
    *   **STATUS**: Implementado. Esta é a nossa solução atual e funcional.

2.  **Migração Futura (Opcional):**
    *   Após a conclusão da Prioridade 2 (Gerenciamento de Listas com API v4), a exibição também poderá ser migrada para usar o endpoint `GET /4/list/{list_id}`. Este endpoint da v4 é paginado e retorna dados corretos, o que tornaria a nossa estratégia de "busca em duas etapas" desnecessária, simplificando o código e melhorando a performance.

---

### **Prioridade 4: Autenticação Legada (API v3) - CONCLUÍDA (Manter até a migração)**
**Objetivo:** Manter o fluxo de login atual funcional até que a migração para a v4 esteja completa.

*   **Lógica de Deep Link para Retorno da Custom Tab**:
    *   **STATUS**: Implementado. `AndroidManifest.xml` configurado. `MainActivity.kt` captura `request_token` v3 e o `LoginViewModel` o utiliza para criar uma `session_id`.

---

### **Prioridade 5: Melhorias e Funcionalidades Futuras - PENDENTE**
**Objetivo:** Refinar a experiência do usuário e adicionar funcionalidades secundárias.

*   **Feedback Visual:** Adicionar indicadores de carregamento (`CircularProgressIndicator`) e `Snackbar` para mensagens de sucesso/erro.
*   **Gerenciamento de Múltiplas Listas:** Permitir que usuários criem e gerenciem múltiplas listas personalizadas.
*   **Cache Offline:** Implementar cache para a "Minha Lista" para visualização offline.