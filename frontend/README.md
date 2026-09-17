# Cidade em Foco — Frontend

Interface web do **Cidade em Foco**, um MVP acadêmico para moradores de Praia Grande-SP registrarem ocorrências urbanas e consultarem alertas climáticos demonstrativos. A aplicação possui áreas de cidadão e administrador, mapas interativos e interface responsiva em português.

## Tecnologias

As faixas abaixo são as declaradas em [package.json](package.json); as versões resolvidas ficam em [package-lock.json](package-lock.json).

| Tecnologia | Versão declarada | Uso |
| --- | --- | --- |
| React e React DOM | `^19.2.8` | Componentes, estado e renderização da interface |
| React Router | `^8.3.0` | Navegação com `BrowserRouter` e rotas por perfil |
| Vite | `^8.2.0` | Servidor de desenvolvimento e build |
| Plugin React para Vite | `^6.0.4` | Integração do React com o Vite |
| Leaflet / React Leaflet | `^1.9.4` / `^5.0.0` | Mapas, marcadores e seleção de localização |
| Lucide React | `^1.33.0` | Ícones |
| Oxlint | `^1.75.0` | Análise estática, incluindo regras de hooks |

O código usa **JavaScript com JSX**, módulos ES e CSS próprio. A sessão é compartilhada por Context API e pelo hook `useAuth`. A comunicação HTTP usa `fetch`, `FormData` e `Blob` nativos do navegador. Os pacotes `@types/react` e `@types/react-dom` auxiliam as ferramentas de desenvolvimento; não há código TypeScript nesta versão.

## Requisitos

- Node.js compatível com as dependências instaladas: `^20.19.0 || >=22.12.0`, conforme os metadados locais de Vite, plugin React e Oxlint.
- npm, distribuído com o Node.js.
- Backend do projeto configurado e executando, normalmente em `http://localhost:8080`.
- Navegador com JavaScript e acesso à internet para os mapas e a busca de endereços.

Consulte o [README do backend](../README-BACKEND.md) para configurar Java 21, MySQL, JWT, executar a API e preparar uma conta administrativa. O frontend não se conecta diretamente ao banco de dados.

## Instalação e configuração

A partir da raiz do repositório, execute no PowerShell:

```powershell
cd frontend
npm.cmd ci
if (-not (Test-Path .env.local)) {
    Copy-Item .env.example .env.local
}
```

`npm ci` instala as versões do lockfile. Para adicionar ou atualizar dependências intencionalmente, use `npm install` e mantenha o lockfile atualizado. No Linux/macOS, use `npm` no lugar de `npm.cmd` e copie o arquivo com `cp -n .env.example .env.local`.

Edite `.env.local` conforme seu ambiente:

```env
VITE_API_URL=http://localhost:8080
```

| Variável | Obrigatória | Finalidade |
| --- | --- | --- |
| `VITE_API_URL` | Sim | URL base do backend, sem `/api` e sem barra final |

O valor acima é o exemplo de desenvolvimento; **não há fallback no código** de [src/services/api.js](src/services/api.js). Os serviços acrescentam caminhos como `/api/auth/login` à URL configurada.

Reinicie o Vite após alterar o ambiente. As variáveis `VITE_*` são incorporadas ao código entregue ao navegador: use-as para configurações públicas, nunca para senhas de banco ou o segredo JWT. Para mudar a API de um build já gerado, configure a variável e gere o build novamente.

No backend, `CORS_ALLOWED_ORIGINS` deve incluir a origem exata do frontend. O padrão é `http://localhost:5173`; se usar outra porta, outro host ou a prévia em `http://localhost:4173`, ajuste essa configuração antes de iniciar a API. O Vite não possui proxy de API configurado neste projeto.

## Execução

Com o backend ativo em outro terminal, execute dentro de `frontend/`:

```powershell
npm.cmd run dev
```

Acesse a URL exibida no terminal, normalmente `http://localhost:5173`. Caso a porta esteja ocupada, confira a porta escolhida pelo Vite e ajuste o CORS do backend.

Todos os comandos abaixo também partem de `frontend/`:

| Comando | Finalidade |
| --- | --- |
| `npm.cmd run dev` | Iniciar o desenvolvimento com atualização automática |
| `npm.cmd run lint` | Executar o Oxlint com `.oxlintrc.json` |
| `npm.cmd run build` | Gerar os arquivos estáticos em `dist/` |
| `npm.cmd run preview` | Servir localmente o build de `dist/`, normalmente na porta 4173 |

Para conferir o build:

```powershell
npm.cmd run lint
npm.cmd run build
npm.cmd run preview
```

Se preferir permanecer na raiz do repositório, use `npm.cmd --prefix frontend run dev` e o mesmo prefixo para `lint`, `build` e `preview`.

Na hospedagem, publique o conteúdo de `dist/` e configure o servidor para entregar `index.html` nas rotas da aplicação, como `/app/ocorrencias/1`, permitindo acesso direto e atualização da página com `BrowserRouter`. A configuração atual pressupõe hospedagem na raiz do domínio, inclusive para imagens em `/brand` e `/images`. `preview` serve para conferência local do build.

## Estrutura de pastas

```text
frontend/
├── public/
│   ├── brand/                 # Logos e ícones da aplicação
│   ├── images/                # Imagens institucionais e da orla
│   └── favicon.png
├── src/
│   ├── components/
│   │   ├── alerts/            # Banner e cartões de alertas
│   │   ├── map/               # Mapas, busca de endereço e filtro de categorias
│   │   ├── occurrences/       # Classificação, risco, imagens, revisão e cartões
│   │   └── ui/                # Button, Brand, FeedbackState e StatusBadge
│   ├── constants/             # Tipos, categorias, riscos, status e apresentação
│   ├── contexts/              # AuthContext e AuthProvider
│   ├── hooks/                 # useAuth
│   ├── layouts/               # Layouts público, cidadão e administrativo
│   ├── pages/                 # Páginas e seus estilos específicos
│   ├── routes/                # ProtectedRoute e PublicOnlyRoute
│   ├── services/              # Cliente HTTP, autenticação, ocorrências e alertas;
│   │                         # administração e geocodificação
│   ├── styles/                # ui.css e layouts.css compartilhados
│   ├── utils/                 # Formatação de datas em pt-BR
│   ├── App.jsx               # Declaração das rotas
│   ├── main.jsx              # Entrada, BrowserRouter, AuthProvider e CSS global
│   └── index.css             # Cores, tipografia e estilos globais
├── .env.example              # Modelo de configuração pública
├── .gitignore
├── .oxlintrc.json             # Regras de análise estática
├── index.html                # Documento HTML e metadados
├── package.json
├── package-lock.json
├── vite.config.js
└── README.md
```

`.env.local`, `node_modules/` e `dist/` são arquivos ou diretórios locais de configuração, dependências e saída do build. Os componentes básicos em `components/ui` são de apresentação e não consultam a API. As páginas coordenam os fluxos e os serviços encapsulam as requisições; componentes especializados, como a busca de endereços e a galeria, também consomem serviços. O CSS combina estilos globais, compartilhados e específicos de cada tela, com adaptações para celular e computador.

## Rotas e autenticação

| Rota | Acesso | Tela |
| --- | --- | --- |
| `/` | Público | Redirecionamento para o login |
| `/login` | Sem sessão | Login por e-mail e senha |
| `/cadastro` | Sem sessão | Cadastro de cidadão |
| `/app` | `CITIZEN` | Dashboard do cidadão |
| `/app/ocorrencias` | `CITIZEN` | Todas ou minhas ocorrências |
| `/app/ocorrencias/nova` | `CITIZEN` | Registro de ocorrência |
| `/app/ocorrencias/:id` | `CITIZEN` | Detalhes da ocorrência |
| `/app/alertas` | `CITIZEN` | Alertas ativos |
| `/app/perfil` | `CITIZEN` | Perfil e participação |
| `/admin` | `ADMIN` | Dashboard administrativo |
| `/admin/ocorrencias` | `ADMIN` | Gestão de ocorrências |
| `/admin/ocorrencias/:id` | `ADMIN` | Detalhes e alteração de status |
| `/admin/alertas` | `ADMIN` | Gestão de alertas |
| Demais caminhos | Qualquer | Página não encontrada |

O cadastro usa `POST /api/auth/register`, solicita nome, e-mail e senha e cria somente cidadãos. Após o cadastro, o usuário é encaminhado ao login. A senha de cadastro aceita entre 8 e 72 caracteres no formulário.

O login usa `POST /api/auth/login`. O `AuthProvider` guarda JWT, dados públicos do usuário e vencimento no `localStorage`, sob a chave `cidade-em-foco:auth`, para restaurar a sessão após atualizar a página. A senha não é persistida. O logout e o vencimento removem a sessão; as telas que recebem erro 401 também encerram o acesso. Não há fluxo de renovação automática do token.

Sem sessão, as rotas protegidas redirecionam para `/login`. Usuários com outro perfil são enviados à própria área; quem já está autenticado também é redirecionado ao acessar login ou cadastro. As permissões da API são verificadas pelo backend. Para acessar como administrador, prepare a conta conforme o [guia do backend](../README-BACKEND.md#autenticação-e-administrador) e faça novo login após a alteração do perfil.

## Funcionalidades do cidadão

### Dashboard e consulta

A página inicial carrega em paralelo `GET /api/alerts/active` e `GET /api/occurrences`, exibindo um banner com o primeiro alerta retornado, acesso à lista de alertas, mapa com filtros por categoria e até cinco ocorrências recentes.

A listagem permite consultar todas as ocorrências com filtros de categoria, tipo, status e bairro (`category`, `type`, `status`, `neighborhood`) ou alternar para **Minhas ocorrências**, via `GET /api/occurrences/mine`. Os filtros aparecem apenas no modo de todas as ocorrências. Os detalhes mostram descrição, classificação, risco percebido, data de registro, coordenadas, mapa e imagens.

### Registro de ocorrências

O formulário organiza o registro em classificação, localização e imagens, seguido de uma janela de revisão antes da confirmação.

- **Eventos naturais:** alagamento, enchente, queda de árvore, deslizamento, ventos fortes, ressaca marítima, incêndio, chuva intensa e outro.
- **Infraestrutura urbana:** buraco na rua, bueiro entupido, poste danificado, semáforo com problema, rua bloqueada, falta de iluminação, árvore obstruindo a via e outro.
- **Risco percebido:** baixo, médio ou alto; representa a percepção do cidadão, não uma classificação técnica.
- **Descrição:** obrigatória, com até 2.000 caracteres.
- **Localização:** latitude e longitude obrigatórias; pesquisa por endereço, clique no mapa, marcador arrastável, geolocalização do navegador ou edição das coordenadas. Bairro é opcional.

Ao escolher tipo e localização, o frontend consulta ocorrências do mesmo tipo e avisa se encontrar uma a até **300 metros**, cujo status seja diferente de `RESOLVIDA`. O aviso permite abrir o registro semelhante e não bloqueia um novo envio.

### Imagens e envio

São permitidas até **quatro imagens opcionais**, JPEG, PNG ou WebP, com até **5 MB por arquivo**, prévia e remoção antes do envio. O seletor também solicita o uso da câmera em dispositivos compatíveis.

O envio ocorre em duas etapas:

1. `POST /api/occurrences` cria o registro com JSON e retorna seu ID.
2. Se houver imagens, `POST /api/occurrences/{id}/images` envia `multipart/form-data`, repetindo o campo `files` para cada arquivo.

Se apenas o upload falhar, a ocorrência permanece criada e a tela de detalhes recebe um aviso; em caso de 401, a sessão é encerrada. A galeria busca as imagens com JWT, recebe `Blob` e cria URLs temporárias para exibição. Ela usa `imageUrls` e mantém compatibilidade com o campo anterior `imageUrl`. O serviço também conserva a função de upload individual em `POST /api/occurrences/{id}/image`, com campo `file`, mas o formulário atual usa o endpoint plural.

### Alertas e perfil

`/app/alertas` mostra os alertas ativos e dentro da validade retornados por `GET /api/alerts/active`, incluindo tipo, severidade, descrição, início, fim e aviso demonstrativo. São cadastrados manualmente; a aplicação não consulta serviços meteorológicos nem produz previsão própria, e os alertas não substituem avisos oficiais.

`/app/perfil` exibe nome, e-mail, perfil de acesso e data de cadastro recebidos no login, além de totais, ocorrências em andamento, resolvidas e até três registros recentes do cidadão. A tela consulta `GET /api/occurrences/mine`. Edição de perfil e alteração de senha não estão disponíveis no MVP.

## Funcionalidades administrativas

### Dashboard

`/admin` consulta em paralelo `GET /api/admin/occurrences` e `GET /api/admin/alerts`. As respostas alimentam o total de ocorrências, contagens e distribuição visual dos cinco status, mapa, até cinco ocorrências recentes e até três alertas recentes. A contagem de alertas considera os marcados como ativos, independentemente de estarem dentro do período de validade.

O dashboard é de consulta; alterações ficam nas telas de gestão.

### Gestão de ocorrências

`/admin/ocorrencias` oferece filtros de categoria, tipo, status e bairro, com listagem em tabela e acesso ao detalhe por ID. O detalhe consulta `GET /api/admin/occurrences/{id}` e apresenta descrição, risco percebido, datas de criação e atualização, localização e imagens.

A alteração de status envia `PATCH /api/admin/occurrences/{id}/status`, com JSON no formato `{ "status": "EM_ANALISE" }`. Os valores disponíveis são:

| Valor | Exibição |
| --- | --- |
| `REGISTRADA` | Registrada |
| `EM_ANALISE` | Em análise |
| `EM_ATENDIMENTO` | Em atendimento |
| `RESOLVIDA` | Resolvida |
| `NAO_CONFIRMADA` | Não confirmada |

### Gestão de alertas

`/admin/alertas` permite listar todos os alertas, criar um alerta inicialmente inativo e ativá-lo ou desativá-lo. A interface distingue períodos agendados, vigentes e encerrados.

| Método e endpoint | Ação |
| --- | --- |
| `GET /api/admin/alerts` | Listar alertas |
| `POST /api/admin/alerts` | Criar alerta |
| `PATCH /api/admin/alerts/{id}/activate` | Ativar |
| `PATCH /api/admin/alerts/{id}/deactivate` | Desativar |

O formulário recebe título, tipo, severidade, descrição e período. Os tipos são chuva intensa, alagamento, ventos fortes, ressaca marítima, deslizamento e outro; as severidades são baixa, moderada e alta. O fim deve ser posterior ao início. As datas digitadas no horário local do navegador são convertidas para ISO antes do envio, e a exibição usa `pt-BR`. Ativar um alerta não modifica sua validade.

## Mapas e serviços externos

Os mapas usam Leaflet e React Leaflet, com centro inicial em Praia Grande (`-24.005833, -46.405833`) e tiles de `https://tile.openstreetmap.org/{z}/{x}/{y}.png`, mantendo a atribuição ao OpenStreetMap. Marcadores possuem ícones por tipo, risco, status e link para os detalhes. Não há download de mapas para uso offline.

A busca de endereços e a geocodificação reversa são feitas diretamente pelo navegador no Nominatim (`https://nominatim.openstreetmap.org`), em [geocodingService.js](src/services/geocodingService.js). A busca começa com três caracteres, após uma pausa de 450 ms, e solicita até cinco resultados delimitados à região de Praia Grande. Endereços pesquisados e coordenadas usadas na geocodificação são enviados a esse serviço externo; o JWT é acrescentado somente às requisições da API da aplicação.

A localização atual é solicitada ao navegador apenas ao acionar **Usar minha localização**. Se a localização falhar, o usuário pode marcar o mapa; se a geocodificação reversa falhar, as coordenadas selecionadas são mantidas e a interface exibe um aviso.

## Verificação e problemas comuns

O frontend não possui suíte automatizada própria nem script `test`. As verificações disponíveis são `npm.cmd run lint`, `npm.cmd run build` e a validação manual com a API ativa. Os testes de regras e permissões do backend são descritos no [README do backend](../README-BACKEND.md#testes).

Com frontend e backend em execução, valide no navegador:

1. Cadastro, login, restauração da sessão, logout e bloqueio das rotas por perfil.
2. Registro sem imagens, com uma imagem e com quatro; validação de formato, tamanho e limite de arquivos.
3. Busca de endereço, clique e arraste no mapa, localização atual e edição das coordenadas.
4. Aviso de ocorrência semelhante, revisão antes do envio e aviso quando apenas o upload falha.
5. Mapa, filtros, detalhes, galeria, minhas ocorrências e dados do perfil.
6. Alertas ativos e ausência de alertas fora da validade.
7. Login administrativo, dashboard, filtros e alteração de status.
8. Criação, ativação e desativação de alertas e validação das datas.
9. Navegação em celular e computador, inclusive menu administrativo.
10. Estados de carregamento, lista vazia, validação de campos, página inexistente e erro de conexão.

| Sintoma | O que conferir |
| --- | --- |
| Falha ao conectar à API | Backend ativo, `VITE_API_URL` definida e Vite reiniciado após mudanças |
| Erro de CORS | Origem exata do navegador incluída em `CORS_ALLOWED_ORIGINS` no backend |
| Comando npm bloqueado pelo PowerShell | Usar `npm.cmd`, como nos exemplos |
| Retorno ao login | Vencimento da sessão ou resposta 401; autenticar novamente |
| Mapa ou sugestões indisponíveis | Conexão e acesso aos serviços OpenStreetMap/Nominatim |
| Imagens não carregam | Sessão válida, resposta da API e disponibilidade dos arquivos no backend |
| Alerta ativado não aparece para o cidadão | Início e fim da validade, além do estado ativo |
| Erro ao atualizar uma rota na hospedagem | Configuração de fallback para `index.html` |
