# Plano de Ação: Autenticação com API v4

Este documento detalha o plano para implementar o fluxo de autenticação completo com a API v4 do The Movie Database (TMDB), a fim de obter permissões de escrita para gerenciar listas de usuários.

---

### **Plano Proposto**

#### 1. Refatoração do Acesso à API
**Objetivo:** Modificar a base de acesso à rede para suportar tokens de autorização dinâmicos.

*   **Remover Header Estático:** O interceptor em `RetrofitHelperV4.kt` que adiciona o `Authorization` header de forma estática será removido.
*   **Injeção Dinâmica de Token:** A interface `FilmeAPIV4.kt` será alterada. Cada método passará a receber o `Authorization` header como um parâmetro (`@Header("Authorization") authHeader: String`). Isso permitirá usar dinamicamente o `API_READ_ACCESS_TOKEN` para o fluxo de autenticação e o `access_token` do usuário para as operações de escrita.

---

#### 2. Ajuste no Repositório
**Objetivo:** Adaptar a camada de repositório para lidar com a nova assinatura dos métodos da API.

*   O `FilmeRepositoryV4.kt` será atualizado para que suas funções aceitem o token como parâmetro e o repassem para as chamadas da `FilmeAPIV4`.

---

#### 3. Implementação do Fluxo de Autenticação na UI
**Objetivo:** Guiar o usuário pelo processo de autorização e capturar o token de acesso com permissão de escrita.

*   **Passo A (Verificar Token):**
    *   Ao iniciar o fluxo, a aplicação verificará se um `access_token` já está salvo no `UserPreferencesRepository`.

*   **Passo B (Iniciar Autorização):**
    *   Caso não haja token, o fluxo de autorização será iniciado:
        1.  Uma chamada à API criará um `request_token`.
        2.  Usaremos um `Intent` para abrir o navegador do usuário na URL de autorização do TMDB: `https://www.themoviedb.org/auth/access?request_token={REQUEST_TOKEN}&redirect_to=netflixapp://auth`.
        3.  *Contingência:* Se a abordagem com `Intent` não for eficaz ou performática, buscaremos uma alternativa melhor (como Chrome Custom Tabs).

*   **Passo C (Capturar Redirecionamento - Deep Link):**
    *   Após o usuário aprovar a permissão no site do TMDB, ele deve ser **imediatamente redirecionado** de volta para a tela principal do aplicativo (`MainActivity`, conforme configurado na navegação em `NetflixApp.kt`).
    *   Capturaremos o retorno do `request_token` aprovado através do método `onNewIntent` na `MainActivity`.
    *   *Contingência:* Se `onNewIntent` não se provar eficaz nos testes, investigaremos e implementaremos outra abordagem para garantir a captura do redirecionamento.

*   **Passo D (Obter Access Token):**
    *   Com o `request_token` aprovado em mãos, faremos a chamada final para trocá-lo pelo `access_token` permanente com permissão de escrita.

*   **Passo E (Salvar e Usar):**
    *   O `access_token` obtido será salvo de forma segura no `DataStore` usando o `UserPreferencesRepository`.
    *   Finalmente, usaremos este novo token para testar as operações de escrita na API, como `createList` e `addMovie`.
