# Cidade em Foco

![Logo do Cidade em Foco](frontend/public/images/LogoCidadeEmFoco.png)

Plataforma colaborativa para moradores de **Praia Grande-SP** registrarem e acompanharem ocorrências relacionadas a eventos naturais e problemas de infraestrutura urbana. O sistema também permite que administradores publiquem alertas climáticos demonstrativos e acompanhem as ocorrências pelo painel administrativo.

Este é um projeto acadêmico desenvolvido para o **PG Tech Challenge 2026**. O sistema não substitui os canais oficiais da Prefeitura, Defesa Civil ou serviços meteorológicos.

## Visão geral

A aplicação segue uma arquitetura simples:

```text
Navegador
   │
   ▼
Frontend React + Vite
   │  API REST + JWT
   ▼
Backend Spring Boot
   │  JPA + Flyway
   ▼
MySQL
```

O projeto possui dois perfis:

- **CITIZEN:** cadastra e consulta ocorrências, acompanha os próprios registros e visualiza alertas ativos.
- **ADMIN:** acompanha todas as ocorrências, altera seus status e administra alertas climáticos.

### Funcionalidades implementadas

- Cadastro público de cidadãos e autenticação com JWT.
- Separação de acesso entre cidadão e administrador.
- Cadastro, consulta, filtros e detalhes de ocorrências.
- Localização por latitude, longitude, endereço e bairro.
- Mapa interativo de Praia Grande.
- Até quatro imagens por ocorrência, armazenadas localmente.
- Consulta das ocorrências do usuário autenticado.
- Gestão administrativa dos status das ocorrências.
- Criação, ativação e desativação de alertas demonstrativos.
- Dashboard para cidadão e dashboard administrativo.
- Tratamento padronizado de erros e validações.
- Documentação da API com Swagger/OpenAPI.

### Tecnologias

| Camada | Tecnologias principais |
| --- | --- |
| Frontend | React 19, JavaScript, Vite, React Router, Leaflet e CSS |
| Backend | Java 21, Spring Boot 4.1.1, Spring Web MVC, Security, Data JPA e Bean Validation |
| Banco | MySQL 8 e Flyway |
| Autenticação | JWT e BCrypt |
| Documentação | Swagger/OpenAPI |
| Infraestrutura | Docker e Docker Compose |

## Estrutura do repositório

```text
CidadeEmFoco/
├── backend/                 API Spring Boot e Dockerfile
├── frontend/                aplicação React
├── infra/                   Docker Compose e modelo de variáveis
│   ├── docker-compose.yml
│   └── .env.example
├── .gitignore
└── README.md
```

Arquivos com segredos, bancos locais, dependências, builds, uploads e logs são ignorados pelo Git.

## Início rápido

### Requisitos

- JDK 21.
- Node.js `^20.19.0` ou `>=22.12.0`.
- npm.
- MySQL 8, caso não utilize Docker.
- Docker Desktop, opcional para executar backend e banco em containers.

### 1. Executar o backend localmente

Inicie o MySQL e configure as variáveis no PowerShell:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "sua_senha"
$env:JWT_SECRET = "SUA_CHAVE_BASE64_COM_PELO_MENOS_32_BYTES"

cd backend
.\mvnw.cmd spring-boot:run
```

O banco padrão é `cidade_em_foco`. Na primeira execução, o Flyway cria ou atualiza as tabelas automaticamente.

### 2. Executar o frontend

Em outro terminal:

```powershell
cd frontend
npm.cmd install
Copy-Item .env.example .env.local
npm.cmd run dev
```

Endereços padrão:

- Frontend: `http://localhost:5173`
- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Alternativa com Docker

O Docker Compose inicia o backend e o MySQL. O frontend continua sendo executado separadamente com o Vite.

```powershell
cd infra
Copy-Item .env.example .env
```

Edite `infra/.env`, informe uma senha para o banco e uma chave JWT válida. Depois execute:

```powershell
docker compose up --build
```

Para encerrar sem apagar os volumes:

```powershell
docker compose down
```

Para apagar também banco e imagens armazenados nos volumes:

```powershell
docker compose down -v
```

> `docker compose down -v` remove os dados persistidos pelos containers.

## Frontend

O frontend está em `frontend/` e utiliza React com JavaScript. A aplicação é responsiva e possui layouts separados para páginas públicas, área do cidadão e área administrativa.

### Principais telas

| Rota | Perfil | Tela |
| --- | --- | --- |
| `/login` | Público | Login |
| `/cadastro` | Público | Cadastro de cidadão |
| `/app` | CITIZEN | Dashboard do cidadão |
| `/app/ocorrencias` | CITIZEN | Consulta de ocorrências |
| `/app/ocorrencias/nova` | CITIZEN | Cadastro de ocorrência |
| `/app/ocorrencias/:id` | CITIZEN | Detalhes da ocorrência |
| `/app/alertas` | CITIZEN | Alertas ativos |
| `/app/perfil` | CITIZEN | Perfil e resumo de participação |
| `/admin` | ADMIN | Dashboard administrativo |
| `/admin/ocorrencias` | ADMIN | Gestão de ocorrências |
| `/admin/ocorrencias/:id` | ADMIN | Análise e alteração de status |
| `/admin/alertas` | ADMIN | Gestão de alertas |

### Autenticação no frontend

Após o login, o frontend armazena no `localStorage`:

- token JWT;
- data de expiração;
- informações públicas do usuário.

A senha nunca é armazenada. Rotas protegidas verificam a autenticação e o perfil, mas a autorização definitiva sempre é realizada pelo backend.

### Mapas e localização

Os mapas utilizam Leaflet e dados do OpenStreetMap. A busca de endereços e a geocodificação reversa utilizam o Nominatim diretamente pelo navegador.

O cadastro de ocorrência permite:

- pesquisar um endereço;
- selecionar ou arrastar um ponto no mapa;
- usar a localização do navegador;
- informar coordenadas manualmente.

Esses recursos dependem de conexão com a internet e enviam a pesquisa ou as coordenadas ao serviço externo Nominatim.

### Imagens

O usuário pode anexar até quatro imagens JPEG, PNG ou WebP, com limite de 5 MB por arquivo. A ocorrência é criada primeiro e as imagens são enviadas em seguida por `multipart/form-data`.

As imagens protegidas são carregadas com JWT como `Blob`; por isso, o frontend não utiliza diretamente a URL da imagem em uma tag sem autenticação.

### Configuração do frontend

O arquivo `frontend/.env.local` deve conter:

```env
VITE_API_URL=http://localhost:8080
```

Essa variável é pública e incorporada ao build. Senhas e segredos nunca devem ser colocados em variáveis `VITE_*`.

### Comandos do frontend

```powershell
cd frontend
npm.cmd run dev
npm.cmd run lint
npm.cmd run build
npm.cmd run preview
```

O frontend ainda não possui suíte automatizada de testes. As verificações disponíveis são lint, build e testes manuais dos fluxos.

Mais detalhes estão disponíveis em [frontend/README.md](frontend/README.md).

## Backend

O backend está em `backend/` e segue a separação Controller → Service → Repository. DTOs são utilizados nas entradas e respostas da API, evitando a exposição direta das entidades e de dados sensíveis.

### Estrutura

```text
backend/src/main/java/br/com/cidadeemfoco/
├── config/       segurança, CORS e OpenAPI
├── controller/   endpoints REST
├── dto/          objetos de entrada e saída
├── entity/       entidades JPA
├── enums/        valores controlados do domínio
├── exception/    erros e tratamento global
├── repository/   persistência com Spring Data JPA
├── security/     autenticação JWT
└── service/      regras de negócio
```

### Entidades principais

- `User`: usuário, e-mail, senha criptografada, perfil e preferências de WhatsApp.
- `Occurrence`: classificação, descrição, risco percebido, localização, status, datas e autor.
- `OccurrenceImage`: imagens relacionadas a uma ocorrência.
- `ClimateAlert`: alerta demonstrativo, tipo, severidade, validade e estado de ativação.

### Segurança

- O cadastro público sempre cria um `CITIZEN`.
- Senhas são armazenadas com BCrypt.
- A API não utiliza sessão no servidor.
- Rotas protegidas exigem `Authorization: Bearer TOKEN`.
- Rotas `/api/admin/**` exigem o perfil `ADMIN`.
- O segredo JWT deve ser Base64 válido e representar pelo menos 32 bytes.

### Administrador inicial

Cadastre o usuário normalmente e promova-o diretamente no banco:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'admin@exemplo.com';
```

Faça um novo login após a alteração para gerar um token com o perfil atualizado.

### Endpoints principais

#### Autenticação

| Método | Endpoint | Acesso | Objetivo |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Público | Cadastrar cidadão |
| POST | `/api/auth/login` | Público | Autenticar e gerar JWT |

#### Preferências de WhatsApp

| Método | Endpoint | Acesso | Objetivo |
| --- | --- | --- | --- |
| GET | `/api/users/me/whatsapp` | CITIZEN | Consultar número e consentimento |
| PUT | `/api/users/me/whatsapp` | CITIZEN | Cadastrar número e autorizar notificações |
| DELETE | `/api/users/me/whatsapp` | CITIZEN | Cancelar notificações e remover o número |

O cadastro exige `phoneNumber` e `consentGiven: true`. Números brasileiros com
DDD são normalizados para o padrão internacional E.164, como
`+5513999999999`, e um mesmo número não pode pertencer a dois usuários. Ao
cancelar, o número e a data do consentimento são removidos.

#### Ocorrências

| Método | Endpoint | Acesso | Objetivo |
| --- | --- | --- | --- |
| POST | `/api/occurrences` | CITIZEN | Criar ocorrência |
| GET | `/api/occurrences` | Autenticado | Listar e filtrar ocorrências |
| GET | `/api/occurrences/map` | Autenticado | Listar ocorrências ainda visíveis no mapa |
| GET | `/api/occurrences/mine` | CITIZEN | Listar ocorrências do usuário |
| GET | `/api/occurrences/{id}` | Autenticado | Consultar detalhes |
| POST | `/api/occurrences/{id}/image` | Autor CITIZEN | Enviar uma imagem |
| POST | `/api/occurrences/{id}/images` | Autor CITIZEN | Enviar várias imagens |
| GET | `/api/occurrences/{id}/image` | Autenticado | Obter a primeira imagem |
| GET | `/api/occurrences/{id}/images/{imageId}` | Autenticado | Obter uma imagem específica |
| GET | `/api/admin/occurrences` | ADMIN | Listagem administrativa |
| GET | `/api/admin/occurrences/{id}` | ADMIN | Detalhes administrativos |
| GET | `/api/admin/occurrences/{id}/reports` | ADMIN | Relatos agrupados no caso |
| PATCH | `/api/admin/occurrences/{id}/status` | ADMIN | Alterar status |

A listagem administrativa aceita `category`, `type`, `status`, `neighborhood`, `createdFrom` e `createdTo` como filtros opcionais. As datas usam o formato ISO 8601.

Ao receber o status `RESOLVIDA`, a ocorrência passa a registrar `resolvedAt`. O endpoint do mapa continua retornando esse pin durante 24 horas e depois o omite, sem excluir a ocorrência das listagens ou do histórico. O prazo pode ser alterado por `RESOLVED_MAP_VISIBILITY`.

O backend encerra automaticamente ocorrências temporárias dos tipos `ALAGAMENTO`, `ENCHENTE`, `VENTOS_FORTES`, `RESSACA_MARITIMA` e `CHUVA_INTENSA`. Por padrão, registros ainda abertos recebem o status `RESOLVIDA` seis horas após a criação. A verificação ocorre a cada cinco minutos e não inclui queda de árvore, deslizamento, incêndio, infraestrutura ou o tipo genérico `OUTRO`.

### Agrupamento e força

Ao cadastrar uma ocorrência, o backend procura um caso principal aberto com a mesma categoria e tipo, criado nas últimas duas horas e localizado em um raio de 500 metros. Quando encontra um caso compatível, preserva o novo relato e o associa ao caso existente, aumentando sua `strength`.

As listagens gerais, administrativas e do mapa retornam apenas os casos principais. `GET /api/occurrences/mine` continua retornando os relatos feitos pelo cidadão. Toda resposta de ocorrência informa `caseId`, que identifica o caso principal, e `strength`, que representa a quantidade total de relatos agrupados. Os critérios podem ser alterados pelas variáveis de ambiente sem mudança no código.

`strength` indica somente a quantidade de relatos associados. Ela não representa gravidade técnica, risco oficial ou confirmação da Prefeitura.

#### Alertas

| Método | Endpoint | Acesso | Objetivo |
| --- | --- | --- | --- |
| GET | `/api/alerts/active` | Autenticado | Listar alertas ativos e vigentes |
| GET | `/api/alerts/stream` | Autenticado | Receber avisos SSE de atualização |
| POST | `/api/admin/alerts` | ADMIN | Criar alerta inicialmente inativo |
| GET | `/api/admin/alerts` | ADMIN | Listar todos os alertas |
| GET | `/api/admin/alerts/{id}` | ADMIN | Consultar alerta |
| PATCH | `/api/admin/alerts/{id}/activate` | ADMIN | Ativar alerta |
| PATCH | `/api/admin/alerts/{id}/deactivate` | ADMIN | Desativar alerta |
| DELETE | `/api/admin/alerts/{id}` | ADMIN | Excluir qualquer alerta |

O endpoint `/api/alerts/stream` mantém uma conexão SSE e envia o evento
`alerts-updated` quando um alerta é criado, ativado ou desativado. O evento também
é enviado periodicamente para cobrir o início e o término da vigência. Ao
recebê-lo, o frontend deve consultar novamente `/api/alerts/active`.

O ADMIN pode excluir manualmente qualquer alerta. Além disso, alertas cuja
`endAt` for igual ou anterior ao momento da verificação são excluídos
automaticamente. Por padrão, a limpeza ocorre a cada cinco minutos e também
notifica os clientes conectados ao stream.

A conexão é protegida por JWT. Como o `EventSource` nativo do navegador não
permite adicionar o cabeçalho `Authorization`, o frontend deve consumir o stream
com uma requisição SSE baseada em `fetch` que envie `Authorization: Bearer <token>`.

### Valores do domínio

- Categorias: `EVENTO_NATURAL` e `INFRAESTRUTURA_URBANA`.
- Risco percebido: `BAIXO`, `MEDIO` e `ALTO`.
- Status: `REGISTRADA`, `EM_ANALISE`, `EM_ATENDIMENTO`, `RESOLVIDA` e `NAO_CONFIRMADA`.
- Severidade dos alertas: `BAIXA`, `MODERADA` e `ALTA`.

Tipos naturais:

- `ALAGAMENTO`, `ENCHENTE`, `QUEDA_ARVORE`, `DESLIZAMENTO`, `VENTOS_FORTES`, `RESSACA_MARITIMA`, `INCENDIO`, `CHUVA_INTENSA` e `OUTRO`.

Tipos de infraestrutura:

- `BURACO_RUA`, `BUEIRO_ENTUPIDO`, `POSTE_DANIFICADO`, `SEMAFORO_COM_PROBLEMA`, `RUA_BLOQUEADA`, `FALTA_ILUMINACAO`, `ARVORE_OBSTRUINDO_VIA` e `OUTRO`.

O risco é apenas a percepção do cidadão e não representa uma classificação técnica oficial.

### Banco e migrations

O backend usa MySQL e valida o schema com Hibernate. Mudanças estruturais são aplicadas pelo Flyway a partir de:

```text
backend/src/main/resources/db/migration/
```

As migrations existentes criam as tabelas principais e adicionam múltiplas imagens, controle de resolução, agrupamento de relatos e preferências de WhatsApp.

### Variáveis do backend

| Variável | Padrão | Finalidade |
| --- | --- | --- |
| `DB_URL` | MySQL local `cidade_em_foco` | URL JDBC |
| `DB_USERNAME` | `root` | Usuário do banco |
| `DB_PASSWORD` | vazio | Senha do banco |
| `JWT_SECRET` | sem padrão | Segredo JWT em Base64 |
| `JWT_EXPIRATION_MINUTES` | `1440` | Validade do token |
| `SERVER_PORT` | `8080` | Porta da API |
| `OCCURRENCE_IMAGE_DIR` | `uploads/occurrences` | Diretório das imagens |
| `RESOLVED_MAP_VISIBILITY` | `24h` | Tempo que um pin resolvido permanece no mapa |
| `TEMPORARY_EVENT_LIFETIME` | `6h` | Tempo até o encerramento de um evento temporário |
| `AUTO_RESOLUTION_INTERVAL` | `5m` | Intervalo entre verificações automáticas |
| `AUTO_RESOLUTION_INITIAL_DELAY` | `5m` | Espera inicial antes da primeira verificação |
| `OCCURRENCE_GROUPING_WINDOW` | `2h` | Janela de tempo para agrupar relatos |
| `OCCURRENCE_GROUPING_RADIUS_METERS` | `500` | Distância máxima entre relatos agrupados |
| `ALERT_REALTIME_CONNECTION_TIMEOUT` | `30m` | Duração máxima de cada conexão SSE |
| `ALERT_REALTIME_REFRESH_INTERVAL` | `30s` | Intervalo da sinalização periódica de alertas |
| `ALERT_CLEANUP_INTERVAL` | `5m` | Intervalo da exclusão automática de alertas expirados |
| `ALERT_CLEANUP_INITIAL_DELAY` | `5m` | Espera inicial antes da primeira limpeza |
| `CORS_ALLOWED_ORIGINS` | localhost nas portas de desenvolvimento | Origens permitidas |

O arquivo `infra/.env` é carregado pelo Docker Compose. Ao executar o backend diretamente com Maven ou pela IDE, configure as variáveis no terminal ou na configuração de execução.

### Erros

A API retorna respostas JSON padronizadas para:

- validações;
- credenciais inválidas;
- ausência de autenticação;
- acesso proibido;
- recurso não encontrado;
- regras de negócio;
- falhas no armazenamento de imagens.

### Testes do backend

```powershell
cd backend
.\mvnw.cmd test
```

A suíte atual possui 96 testes cobrindo domínio, serviços, controllers, JWT, permissões administrativas, preferências de WhatsApp, alertas em tempo real, limpeza de alertas expirados, filtros, visibilidade no mapa, encerramento automático, agrupamento e armazenamento de imagens.

## Roadmap de evolução

Os itens abaixo estão planejados, mas ainda não devem ser considerados implementados:

- Publicação da API e do banco em ambiente remoto.
- Fila de notificações e integração com a WhatsApp Cloud API. O cadastro e o consentimento do usuário já estão implementados.

## Limitações atuais

- As imagens ficam no disco local ou em volume Docker; não há armazenamento em nuvem.
- Não existe integração oficial com Prefeitura ou Defesa Civil.
- Os alertas são cadastrados manualmente e não substituem fontes oficiais.
- O agrupamento atual não possui moderação antifraude ou bloqueio de relatos repetidos pelo mesmo usuário.
- O número e o consentimento do WhatsApp já podem ser cadastrados, mas o envio externo ainda não está conectado à Meta.
- Não existe previsão meteorológica própria, IA, SMS, IoT ou aplicativo nativo.
- O frontend depende de serviços externos do OpenStreetMap para mapas e geocodificação.

## Aviso

O **Cidade em Foco** é um projeto acadêmico em evolução. Em situações reais de risco, consulte os canais oficiais da Prefeitura de Praia Grande, Defesa Civil e serviços de emergência.
