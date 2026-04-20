---
name: minimal-architecture-guidelines
description: Garantir que todas as alterações, novas telas e funções sigam a arquitetura MVVM + Compose + Room do projeto.
---

# Minimal Architecture Guidelines

Este guia define os padrões e convenções obrigatórios para o projeto Minimal. Todas as modificações devem seguir estas regras para garantir consistência e manutenibilidade.

## Quando usar esta skill

- Sempre que criar uma nova tela (UI Page).
- Ao adicionar novas funcionalidades ou operações na camada de dados.
- Ao modificar ViewModels ou lógica de negócio existente.
- Para garantir que o Fluxo de Dados Unidirecional (UDF) seja mantido.

## Princípios Core

### 1. Padrão MVVM (Separação Estrita)
- **View (Compose):** Deve lidar apenas com a UI e reagir ao estado. Sem acesso direto ao banco de dados.
- **ViewModel:** Deve manter o estado da UI usando `StateFlow`. Orquestra transformações e coordena com os DAOs.
- **Model (Room):** Os dados devem ser persistidos no Room. Use entidades para representação e DAOs para acesso.

### 2. Gestão de Estado (UDF)
- Todo o estado da UI deve ser representado por uma `sealed class` (ex: `AccountState`).
- ViewModels devem expor um `StateFlow` público e manter um `MutableStateFlow` privado.
- Composables devem coletar o estado usando `collectAsState()`.

### 3. Persistência de Dados
- **Room é a Fonte Única de Verdade:** Nunca armazene dados primários apenas em memória se eles devem ser persistentes.
- Use funções `suspend` nos DAOs para operações assíncronas.
- Execute operações de banco de dados sempre em `Dispatchers.IO` dentro do ViewModel usando `viewModelScope.launch`.

### 4. Convenções de Código
- **Pages:** Localizadas em `ui.pages`.
- **ViewModels:** Localizadas em `ui.viewmodel`.
- **Entities/DAOs:** Localizadas nos pacotes `entity` e `dao`.
- **Naming:** Seguir convenções do projeto (ex: `XPage.kt`, `XViewModel.kt`, `XDao.kt`).

## Passo a Passo para Novas Funcionalidades

1.  **Definir a Entidade:** Criar ou atualizar a Entity Room em `entity/`.
2.  **Atualizar Acesso:** Adicionar métodos necessários ao DAO em `dao/`.
3.  **Definir o Estado:** Criar a sealed class para o novo estado no ViewModel.
4.  **Implementar Lógica:** Adicionar a lógica de negócio no `ViewModel` usando Coroutines e atualizando o `StateFlow`.
5.  **Construir a UI:** Criar o Composable Page em `ui/pages/` que consome o estado e dispara eventos.
