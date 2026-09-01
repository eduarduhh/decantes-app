# Decantes

App Android nativo (Kotlin + Jetpack Compose) para controlar rateios de compras em
grupo de perfumes/decants: quanto cada perfume custou, quanto já foi pago e quanto
ainda falta pagar. 100% local, sem backend — persistência em Room e backup/restore
manual via JSON (Storage Access Framework).

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite local) — `data/AppDatabase.kt`
- Navigation Compose — `ui/nav/NavGraph.kt`
- kotlinx.serialization para o JSON de backup
- minSdk 24, targetSdk 34, compileSdk 34
- AGP 8.7.3, Kotlin 1.9.24, Compose BOM 2024.09.00, kotlinx-serialization-json 1.6.3
  (fixado nessa versão porque 1.7.3+ exige Kotlin 2.0)

## Estrutura de pacotes

```
com.eduarduhh.decantes/
├── data/
│   ├── entity/        Grupo, Perfume (com `marca: String`, opcional), Pagamento
│   ├── dao/            GrupoDao, PerfumeDao, PagamentoDao
│   ├── relation/        GrupoComPerfumes, PerfumeComPagamentos (+ enum StatusPerfume)
│   ├── backup/          BackupModels.kt (DTOs @Serializable p/ export/import)
│   ├── repository/      DecantesRepository.kt (única porta de entrada p/ as telas)
│   └── AppDatabase.kt
├── viewmodel/            HomeViewModel, GroupListViewModel, GroupDetailViewModel,
│                         PerfumeDetailViewModel, BackupViewModel, ViewModelFactory
├── ui/
│   ├── theme/            Color.kt, Theme.kt, Type.kt
│   ├── nav/              Screen.kt, NavGraph.kt
│   ├── components/       Formatters.kt, PagamentoDialog, ConfirmDialog, StatusBadge
│   ├── home/              HomeScreen.kt
│   ├── groups/            GroupListScreen.kt
│   ├── groupdetail/        GroupDetailScreen.kt
│   └── perfumedetail/      PerfumeDetailScreen.kt
│   ├── backup/             BackupScreen.kt
│   └── lock/               AppLock.kt (bloqueio biométrico, ver seção própria)
└── MainActivity.kt
```

Regra de negócio central (em `PerfumeComPagamentos`, `data/relation/`):
`saldoRestante = valorTotal - soma(pagamentos)`; status = PENDENTE (nada pago),
PARCIAL (0 < pago < total) ou QUITADO (pago >= total).

## Estado atual (tudo funcionando, testado no emulador e em aparelho físico)

- Home: **só financeiro** — card com total geral devido em destaque + lista de
  cards por grupo (nome + saldo daquele grupo, clicável). Sem lista de
  perfumes nem "Dar baixa" aqui — isso foi removido de propósito a pedido do
  usuário; tocar num grupo navega pro Detalhe do Grupo.
- Lista de Grupos: criar grupo (só nome), resumo pago/total por grupo.
- Detalhe do Grupo: card no topo com totalizador só daquele grupo (pago/total e
  saldo). Abaixo dele, controles de lista: toggle "Mostrar quitados" (esconde
  perfumes com status QUITADO), lupa que abre um campo de busca por nome, e
  ordenação (nome/valor total/saldo devedor) via ícone de sort — tudo em
  `GroupDetailViewModel` (`OrdenacaoPerfume`, filtro combinados numa única
  `Filtros` interna, ver o arquivo). O totalizador do topo sempre reflete o
  grupo inteiro, **não** a lista filtrada — só a lista de cards abaixo respeita
  filtro/busca (a busca bate em nome **e** marca). Lista de perfumes
  (editar/excluir cada um, editar/excluir grupo); tocar num perfume abre o
  Detalhe do Perfume.
- Perfume tem campo opcional "Marca" (ex: "Dior", "Chanel") — aparece no
  formulário de novo/editar perfume, e quando preenchido é exibido como
  prefixo do nome ("Dior Sauvage · 10ml") nos cards e no título do Detalhe do
  Perfume; sem marca, mostra só o nome como antes. O campo Marca tem
  autocomplete: `PerfumeDao.observarMarcas()` (`SELECT DISTINCT marca`) expõe
  via `DecantesRepository.observarMarcas()` a lista de marcas já cadastradas
  em qualquer perfume/grupo; `GroupDetailViewModel` inclui `marcasSugeridas`
  no `uiState`, e `PerfumeFormDialog` (em `GroupDetailScreen.kt`) usa um
  `ExposedDropdownMenuBox` que filtra essa lista conforme o usuário digita.
- Detalhe do Perfume: histórico de pagamentos (editar/excluir cada um), botão
  "Lançar pagamento" — é aqui que a baixa acontece agora (não mais na Home).
- Home tem um ícone de "olho" (estilo apps de banco) no card do total geral —
  alterna `valoresOcultos` (estado local via `rememberSaveable` em
  `HomeScreen.kt`, não persiste em disco) e mascara tanto o total geral
  quanto o saldo de cada grupo na lista abaixo, trocando o valor formatado
  por `"R$ ••••••"` (`formatarMoedaOuOculto` em `ui/components/Formatters.kt`).
  Só existe na Home por enquanto — o totalizador do Detalhe do Grupo não tem
  esse toggle ainda.
- Backup/Restore: exporta JSON via `ACTION_CREATE_DOCUMENT`
  (`perfumes_backup_AAAA-MM-DD.json`), importa via `ACTION_OPEN_DOCUMENT` com
  diálogo de confirmação (substitui tudo, dentro de uma transação Room).
- Validação de campos numéricos (ml/valores não podem ser negativos ou vazios),
  teclado numérico/decimal nesses campos.
- Tema Material 3 custom com paleta própria (ver abaixo), light/dark.
- Bloqueio biométrico ao abrir o app (ver seção própria abaixo).

## Paleta de cores (decidida — pode mudar de novo se o usuário pedir)

Definida em `ui/theme/Color.kt` (tokens `PrimaryLight/Dark`,
`SecondaryLight/Dark`, etc. usados em `ui/theme/Theme.kt`). Passamos por
várias paletas ao longo de sessões diferentes — sempre em cima do mesmo
histórico de "o usuário acha uma cor incômoda, pede pra trocar":
vermelho vivo → âmbar/laranja → **paleta atual: fria e leve** (azul suave
como protagonista). O pedido mais recente foi explicitamente por "cores mais
leves e frias" (não mais uma família quente com um erro discordante). Nomes
das variáveis em `Color.kt` foram todos renomeados nessa leva pra bater com
os valores atuais (`AzulSuave`, `AzulAcinzentado`, `VerdeAzulado`,
`TerracotaSuave`) — diferente de trocas anteriores, dessa vez não sobrou nome
desatualizado:

```
primary   (claro)  = #6c93b0  (azul suave)
secondary (claro)  = #5c7e93  (azul mais escuro, texto/ícone legível)
tertiary  (claro)  = #4f7a6d  (verde-azulado, acento)
error     (ambos)  = #b0705a  (terracota suave — só aqui e no badge Pendente;
                      é o único toque quente do app, de propósito, pra
                      diferenciar "atenção" do resto da paleta fria)
background (escuro)= #10181e  (quase preto azulado)
```

Badges de status (`ui/components/StatusBadge.kt`): `AlertaAmarelo` = âmbar
dessaturado #c79a3c (Parcial — mantido âmbar de propósito, é o único
elemento "quente-neutro" fora do error, pra preservar o significado universal
de aviso), `ErroVermelho` = terracota suave (Pendente, mesma cor do error),
`SucessoVerde` fixo #2e7d32 (Quitado — mantido verde, significado universal
de "pago").

Se o usuário pedir pra mexer de novo: **pergunte a direção antes de aplicar**
(já erramos a mão várias vezes — vermelho puro demais, depois frio demais
contra um fundo quente, depois laranja puxando pro vermelho de novo) —
funciona bem usar `AskUserQuestion` com 2-3 opções concretas com preview do
hex; se a resposta do usuário for vaga (ex: "mais leve e frio" sem escolher
uma cor), vale perguntar de novo com as opções já ajustadas antes de aplicar
— não adivinhar o tom exato.

## Bloqueio biométrico

`ui/lock/AppLock.kt` — composable `AppComBloqueio(activity: FragmentActivity, content)`
que envolve todo `DecantesNavGraph` em `MainActivity`. Pede digital/rosto/PIN
(via `androidx.biometric.BiometricPrompt`, com `BIOMETRIC_WEAK or
DEVICE_CREDENTIAL` — aceita PIN/padrão do aparelho como fallback) ao abrir o
app e sempre que ele volta do segundo plano. Se o aparelho não tem biometria
nem tela de bloqueio configurada (`canAuthenticate() != BIOMETRIC_SUCCESS`),
libera direto sem travar — não bloqueia quem não tem como desbloquear.

Detalhes de implementação que importam se for mexer aqui de novo:
- `MainActivity` precisou virar `FragmentActivity` (não `ComponentActivity`)
  — é exigência do `BiometricPrompt`.
- **Bug já corrigido**: chamar `prompt.authenticate()` direto num
  `LaunchedEffect` na composição dá `BiometricPromptCompat: Unable to start
  authentication. Called after onSaveInstanceState()` — a Activity ainda não
  está garantidamente `RESUMED` nesse momento. Corrigido disparando a
  autenticação só no evento `Lifecycle.Event.ON_RESUME` (via
  `LifecycleEventObserver`), e resetando o estado de desbloqueio no
  `ON_STOP` (assim tranca de novo quando o app vai pro histórico/background).
- Dependência: `androidx.biometric:biometric:1.1.0` (`libs.versions.toml` +
  `app/build.gradle.kts`). Não precisou declarar `USE_BIOMETRIC` no
  Manifest — a própria lib faz o merge.

## Esquema do banco (Room) e migrações

`data/AppDatabase.kt` está em **versão 2** (`exportSchema = false`, então não
há pasta de schemas JSON gerada — histórico das mudanças fica só aqui e no
git). Versão 1→2 adicionou a coluna `marca` em `perfumes` via
`MIGRATION_1_2` (`ALTER TABLE perfumes ADD COLUMN marca TEXT NOT NULL DEFAULT
''`), registrada com `.addMigrations(MIGRATION_1_2)` no builder.

**Importante pra próximas mudanças de schema**: o usuário já tem dados reais
no celular físico (grupos/perfumes cadastrados de verdade, não só dados de
teste) — nunca usar `fallbackToDestructiveMigration()` nem pular a
`Migration`, isso apagaria o banco dele. Sempre escrever uma `Migration`
explícita, bump de `version`, e testar reinstalando por cima de uma build
antiga (não só instalando limpo) antes de considerar pronto.

## Ícone do app

Trocado do robô padrão do Android Studio para um frasco de perfume com tampa em
diamante, estilo contorno preto (`#241714`), a pedido do usuário (mandou uma
referência visual). Recriado como vetor nativo — não como imagem importada —
porque **não há rasterizador de SVG disponível no ambiente** (`convert`,
`rsvg-convert`, `inkscape` — nenhum instalado; só `sips`, que não renderiza
vetor). Arquivos:

- `res/drawable/ic_launcher_foreground.xml` — o frasco (linhas pretas,
  fillColor transparente, estilo line-art)
- `res/drawable/ic_launcher_background.xml` — fundo sólido âmbar `#FCAC03`
- `res/mipmap-anydpi-v26/ic_launcher*.xml` — já apontavam pra esses drawables,
  não precisou mexer

**Limitação conhecida**: os ícones raster legados em `res/mipmap-{h,m,xh,xxh,xxxh}dpi/`
(webp) ainda são o robô verde antigo do template — só são usados em Android
< 8.0 (API 24-25), quando o adaptive icon (`mipmap-anydpi-v26`) não se aplica.
Baixíssimo risco na prática (poucos aparelhos nessa faixa hoje), mas se algum
dia surgir uma ferramenta de rasterização no ambiente (ou o usuário quiser
gerar via Android Studio → Image Asset), vale regenerar esses PNGs a partir do
mesmo vetor pra consistência total.

## Como compilar/rodar

```bash
./gradlew :app:assembleDebug
```

Ambiente de teste usado nesta sessão: emulador `Pixel_Fold_API_36`
(`$ANDROID_HOME/emulator/emulator -avd Pixel_Fold_API_36`). É um dispositivo
dobrável com **duas telas** — `adb shell screencap` sem `-d <display-id>` pode
capturar a tela errada (fica preta). Para saber os IDs:
`adb shell dumpsys SurfaceFlinger --display-id`.

Instalar e abrir:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.eduarduhh.decantes/.MainActivity
```

Cold start no emulador é lento (~20s) — espere `ActivityTaskManager: Displayed`
no logcat antes de tirar screenshot, ou o print sai preto/com o launcher.

**Segundo dispositivo**: o usuário também testa num celular físico
conectado por wifi debugging (`adb devices` mostra algo como
`adb-XXXXXXXX-XXXXXX._adb-tls-connect._tcp`, junto do `emulator-5554`).
Sempre que for instalar/testar uma mudança visual ou de fluxo, instalar nos
dois (`for DEV in emulator-5554 "adb-...";  do adb -s "$DEV" install -r
...; done`) — o usuário costuma olhar no celular, não só no emulador.
Cuidados com o celular físico:
- Tela bloqueada faz `adb shell screencap` devolver uma imagem toda preta
  (comportamento normal de privacidade do Android em telas recentes, não é
  bug) — sempre `input keyevent KEYCODE_WAKEUP` antes, e se ainda estiver no
  keyguard, avisar o usuário que precisa desbloquear pra eu conseguir ver.
- Depois de reinstalar, `am start` pode cair num "Warning: Activity not
  started, intent has been delivered to currently running top-most
  instance" se o app não tiver sido morto — usar `am force-stop` antes
  quando for importante garantir um cold start de verdade (ex: testar o
  bloqueio biométrico, que só dispara nesse cenário).

## Coisas para revisar depois (não bloqueiam nada, só notas)

- Uso de `Double` para valores monetários (não `BigDecimal`/centavos em `Long`).
  Aceitável para app pessoal de baixo volume, mas é um ponto de atenção se algum
  dia surgirem somas com muitos itens e diferenças de centavos importarem.
- `GroupListViewModel`, `HomeViewModel` etc. fazem o join perfume↔grupo em
  memória via `combine(...)` de dois Flows do Room em vez de uma query única —
  simples e funciona bem no volume esperado (uso pessoal), mas não escala para
  milhares de linhas.
