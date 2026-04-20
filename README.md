# Minimal

Aplicação Android minimalista para gestão financeira pessoal, permitindo o acompanhamento de saldo e registo de operações de crédito e débito.

## 🏗️ Arquitetura

O projeto utiliza a **Modern Android Architecture**, implementando o padrão **MVVM (Model-View-ViewModel)**. Esta escolha garante uma separação clara de responsabilidades, facilitando a manutenção e a escalabilidade.

### Camadas:
1.  **UI Layer (Presentation):** Construída inteiramente com **Jetpack Compose**. A interface é declarativa e reage automaticamente às mudanças de estado.
2.  **ViewModel Layer:** Atua como o detentor de estado (State Holder). Utiliza `StateFlow` para emitir estados de interface de forma reactiva e lida com a lógica de negócio através de Coroutines.
3.  **Data Layer:** Utiliza **Room Persistence Library** como fonte local de dados (SQLite). As entidades representam as tabelas e os DAOs definem as operações de acesso.

---

## 📚 Validação com a Documentação Oficial

Este projeto foi desenhado seguindo as recomendações do **Android Guide to App Architecture**:

*   **Separação de Preocupações:** A lógica de cálculo e persistência está isolada no `ViewModel`, garantindo que a `Activity` e os `Composables` foquem apenas na renderização.
*   **Fluxo de Dados Unidirecional (UDF):** Implementado através de `MutableStateFlow`. O estado flui do ViewModel para a UI, e os eventos (cliques e inputs) fluem da UI para o ViewModel.
*   **Single Source of Truth (SSOT):** O banco de dados Room é a única fonte de verdade. Qualquer alteração no saldo é primeiro persistida e depois refletida na interface.
*   **Gerenciamento de Ciclo de Vida:** O uso de `ViewModel` da Jetpack garante que os dados sobrevivam a mudanças de configuração (como rotação de ecrã).

---

## 🛠️ Tecnologias Utilizadas

*   **Linguagem:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Persistência:** Room DB
*   **Assincronismo:** Kotlin Coroutines & Flow
*   **Arquitetura:** MVVM (Model-View-ViewModel)

---

## 📂 Estrutura de Pastas

*   `config/`: Configurações globais e instância do banco de dados.
*   `dao/`: Interfaces Room para acesso a dados.
*   `entity/`: Modelos de dados persistentes.
*   `ui/pages/`: Componentes visuais principais (Composables).
*   `ui/viewmodel/`: Lógica de estado e coordenação de dados.
*   `ui/theme/`: Definições de cores, tipografia e estilos.
