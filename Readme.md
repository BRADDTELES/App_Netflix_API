# StreamDTC - App Netflix Clone

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Descrição

StreamDTC é um aplicativo Android desenvolvido como um clone da interface da Netflix, consumindo dados da API do The Movie DB (TMDB). O projeto serve como um estudo prático de desenvolvimento Android moderno, implementando uma arquitetura robusta e utilizando as bibliotecas mais recentes do ecossistema Jetpack e do Material 3 Expressive.

O aplicativo permite que os usuários naveguem por catálogos de filmes e séries, visualizem detalhes, gerenciem listas pessoais e muito mais.

<br>

[![Demonstração do App StreamDTC](https://i.ytimg.com/vi/ZeeMcsxKFPw/hqdefault.jpg)](https://youtube.com/shorts/ZeeMcsxKFPw)

<br>

## ✨ Principais Funcionalidades

-   **Navegação de Conteúdo:** Explore filmes e séries populares, em exibição e mais bem avaliados.
-   **Paginação:** Carregamento infinito de listas de conteúdo para uma navegação fluida, implementado com a biblioteca Paging 3.
-   **Detalhes do Conteúdo:** Visualize informações detalhadas sobre cada filme ou série, como sinopse e vídeos.
-   **Minha Lista:** Funcionalidade para que os usuários possam criar e gerenciar suas próprias listas de favoritos.
-   **Autenticação:** Mecanismos de login para gerenciamento de sessão de usuário.
-   **Persistência de Dados:** Utilização do Jetpack DataStore para armazenar preferências do usuário e dados locais.

## 🛠️ Tecnologias e Arquitetura

O projeto foi construído seguindo as melhores práticas de desenvolvimento Android, com uma arquitetura MVVM (Model-View-ViewModel).

-   **Linguagem:** [Kotlin](https://kotlinlang.org/)
-   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) para uma interface de usuário declarativa e moderna, com aprimoramentos visuais e de interação do [Material 3 Expressive](https://m3.material.io/components/extended-fab/overview).
-   **Arquitetura:** MVVM (Model-View-ViewModel)
-   **Navegação:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) para gerenciar o fluxo de telas do aplicativo.
-   **Comunicação com API:**
    -   [Retrofit](https://square.github.io/retrofit/) para consumir a API v3 do TMDB.
    -   [Ktor Client](https://ktor.io/docs/client-create-new-application.html) para as interações com a API v4 do TMDB.
-   **Assincronismo:** Kotlin Coroutines e Flow para gerenciar operações em background de forma eficiente.
-   **Paginação:** [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) para carregar e exibir grandes listas de dados.
-   **Armazenamento Local:** [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) para persistência de dados chave-valor de forma assíncrona.

## 📂 Estrutura do Projeto

O código-fonte está organizado em pacotes que separam as responsabilidades, seguindo os princípios da Clean Architecture.

-   **/api**: Contém as interfaces do Retrofit e Ktor para a comunicação com a API do TMDB.
-   **/model**: Classes de dados (data classes) que representam as respostas da API.
-   **/repository**: Repositórios que abstraem a origem dos dados (rede ou local).
-   **/viewmodel**: ViewModels responsáveis pela lógica de negócio e por expor o estado para a UI.
-   **/view**: Contém os Composables do Jetpack Compose, que formam as telas do aplicativo.
-   **/view/navigation**: Lógica de navegação do aplicativo com o Navigation Compose.
-   **/datasource**: Fontes de dados, incluindo as implementações do PagingSource e o acesso ao DataStore.
