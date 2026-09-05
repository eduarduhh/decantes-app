# 🧴 Decantes

App Android nativo para controlar rateios de compras em grupo de perfumes e decants: quanto cada perfume custou, quanto já foi pago e quanto ainda falta pagar.

100% local, sem backend, sem conta, sem anúncios — seus dados ficam só no aparelho, com backup/restore manual via JSON quando quiser levar pra outro celular.

## Por que existe

Comprar perfume dividido em grupo é prático, mas controlar "quem já pagou quanto" numa planilha ou num grupo de WhatsApp vira bagunça rápido. O Decantes resolve isso: cada grupo de compra vira uma tela, cada perfume dentro dele mostra o saldo em tempo real, e cada pagamento fica registrado com histórico.

## Funcionalidades

- **Home financeira** — total geral devido em destaque, com lista de saldo por grupo. Toque num grupo pra ver o detalhe.
- **Grupos de compra** — crie quantos grupos quiser (ex: "Pedido Fragrantica Março"), cada um com seu próprio totalizador de pago/total/saldo.
- **Perfumes por grupo** — nome, marca (com autocomplete das marcas já cadastradas), volume e valor total. Busca por nome/marca, ordenação (nome, valor, saldo) e opção de esconder os já quitados.
- **Status automático** — cada perfume é classificado como **Pendente**, **Parcial** ou **Quitado** conforme os pagamentos lançados.
- **Histórico de pagamentos** — lance, edite ou exclua pagamentos por perfume, com data e valor.
- **Ocultar valores** — ícone de "olho" na Home (estilo app de banco) pra esconder os valores na tela na hora.
- **Backup/Restore** — exporta tudo pra um `.json` (Storage Access Framework, você escolhe onde salvar) e importa de volta com confirmação antes de sobrescrever.
- **Bloqueio biométrico** — digital, rosto ou PIN do aparelho ao abrir o app e ao voltar do segundo plano.
- **Tema Material 3** — paleta própria fria e leve, com suporte a modo claro e escuro.

## Stack

| | |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Persistência | Room (SQLite local) |
| Navegação | Navigation Compose |
| Serialização | kotlinx.serialization (backup em JSON) |
| Biometria | androidx.biometric |
| minSdk / targetSdk / compileSdk | 24 / 34 / 36 |

## Arquitetura

MVVM simples, sem camada de rede — a `DecantesRepository` é a única porta de entrada entre as telas e o Room.

```
com.eduarduhh.decantes/
├── data/
│   ├── entity/          Grupo, Perfume, Pagamento
│   ├── dao/              GrupoDao, PerfumeDao, PagamentoDao
│   ├── relation/          GrupoComPerfumes, PerfumeComPagamentos (+ StatusPerfume)
│   ├── backup/            DTOs @Serializable para export/import
│   ├── repository/        DecantesRepository
│   └── AppDatabase.kt
├── viewmodel/              HomeViewModel, GroupListViewModel, GroupDetailViewModel,
│                           PerfumeDetailViewModel, BackupViewModel
├── ui/
│   ├── theme/              paleta e tipografia Material 3
│   ├── nav/                grafo de navegação
│   ├── components/          formatadores, diálogos, badges de status
│   ├── home/ groups/ groupdetail/ perfumedetail/ backup/  telas
│   └── lock/                bloqueio biométrico
└── MainActivity.kt
```

Regra de negócio central: `saldoRestante = valorTotal - soma(pagamentos)`, com status **PENDENTE** (nada pago), **PARCIAL** (parcialmente pago) ou **QUITADO** (pago ≥ total).

## Como rodar

```bash
git clone git@github.com:eduarduhh/decantes-app.git
cd decantes-app
./gradlew :app:assembleDebug
```

Instalar num dispositivo/emulador:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.eduarduhh.decantes/.MainActivity
```

Requer Android Studio (Giraffe+) ou o Android SDK configurado localmente para compilar via Gradle.

## Licença

Projeto pessoal, sem licença de uso comercial definida. Sinta-se à vontade para explorar o código como referência.
