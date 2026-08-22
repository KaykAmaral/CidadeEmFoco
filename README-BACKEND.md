# Cidade em Foco — Backend

API REST do MVP **Cidade em Foco**, uma plataforma acadêmica para moradores de Praia Grande-SP registrarem ocorrências urbanas e consultarem alertas climáticos demonstrativos.

O backend implementa cadastro e autenticação com JWT, ocorrências, foto opcional armazenada localmente, administração de status e alertas climáticos manuais. Integrações oficiais, previsão meteorológica, IA e microserviços não fazem parte do MVP.

## Tecnologias e requisitos

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC, Data JPA, Security e Bean Validation
- MySQL 8
- Flyway
- Maven Wrapper
- JWT
- Swagger/OpenAPI

Para executar, é necessário ter o **JDK 21** e o **MySQL** ativos. Não é necessário instalar o Maven separadamente.

## Configuração local

O código do backend está em `backend/`. As variáveis disponíveis estão documentadas em `backend/.env.example`.

| Variável | Obrigatória | Padrão | Finalidade |
| --- | --- | --- | --- |
| `DB_URL` | Não | Banco `cidade_em_foco` local | URL JDBC do MySQL |
| `DB_USERNAME` | Não | `root` | Usuário do MySQL |
| `DB_PASSWORD` | Depende do MySQL | vazia | Senha do MySQL |
| `JWT_SECRET` | **Sim** | — | Chave Base64 com pelo menos 32 bytes |
| `JWT_EXPIRATION_MINUTES` | Não | `1440` | Validade do token em minutos |
| `SERVER_PORT` | Não | `8080` | Porta da API |
| `OCCURRENCE_IMAGE_DIR` | Não | `uploads/occurrences` | Diretório local das fotos |
| `CORS_ALLOWED_ORIGINS` | Não | `http://localhost:5173` | Origens do frontend, separadas por vírgula |

Exemplo no PowerShell:

```powershell
cd backend

$key = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($key)
$env:JWT_SECRET = [Convert]::ToBase64String($key)

$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "sua_senha"

.\mvnw.cmd spring-boot:run
```

Na primeira execução, o Flyway cria as tabelas. O usuário do MySQL precisa ter permissão para criar o banco caso seja mantido `createDatabaseIfNotExist=true` na URL padrão.

Depois que a aplicação iniciar:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Autenticação e administrador

O cadastro público sempre cria um usuário `CITIZEN`, mesmo que outro perfil seja enviado. Senhas são armazenadas com BCrypt, nunca em texto puro.

Para criar o primeiro administrador de forma simples e preservar a senha criptografada:

1. Cadastre o usuário normalmente em `POST /api/auth/register`.
2. Promova-o diretamente no MySQL:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'admin@exemplo.com';
```

Faça login novamente depois da alteração para receber um token com o perfil atualizado.

Em todas as rotas protegidas, envie o cabeçalho:

```text
Authorization: Bearer SEU_TOKEN_JWT
```

No Swagger, faça o login, copie o campo `token`, clique em **Authorize** e cole somente o token; a interface acrescenta `Bearer`.

## Endpoints

### Autenticação

| Método | Endpoint | Acesso | Objetivo |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Público | Cadastrar cidadão |
| POST | `/api/auth/login` | Público | Autenticar e obter JWT |

### Ocorrências

| Método | Endpoint | Acesso | Objetivo |
| --- | --- | --- | --- |
| POST | `/api/occurrences` | CITIZEN | Criar ocorrência |
| GET | `/api/occurrences` | Autenticado | Listar e filtrar ocorrências |
| GET | `/api/occurrences/mine` | CITIZEN | Listar ocorrências do usuário atual |
| GET | `/api/occurrences/{id}` | Autenticado | Consultar detalhes |
| POST | `/api/occurrences/{id}/image` | CITIZEN dono | Enviar ou substituir a foto |
| GET | `/api/occurrences/{id}/image` | Autenticado | Consultar a foto |
| GET | `/api/admin/occurrences` | ADMIN | Listagem administrativa |
| GET | `/api/admin/occurrences/{id}` | ADMIN | Detalhes administrativos |
| PATCH | `/api/admin/occurrences/{id}/status` | ADMIN | Alterar status |

A listagem aceita os filtros opcionais `category`, `type`, `status` e `neighborhood`. Exemplo:

```text
GET /api/occurrences?category=EVENTO_NATURAL&status=REGISTRADA&neighborhood=Boqueirao
```

### Alertas climáticos

| Método | Endpoint | Acesso | Objetivo |
| --- | --- | --- | --- |
| GET | `/api/alerts/active` | Autenticado | Listar alertas ativos e dentro da validade |
| POST | `/api/admin/alerts` | ADMIN | Criar alerta desativado |
| GET | `/api/admin/alerts` | ADMIN | Listar todos os alertas |
| GET | `/api/admin/alerts/{id}` | ADMIN | Consultar alerta |
| PATCH | `/api/admin/alerts/{id}/activate` | ADMIN | Ativar alerta |
| PATCH | `/api/admin/alerts/{id}/deactivate` | ADMIN | Desativar alerta |

Os alertas são demonstrativos e cadastrados manualmente; não substituem informações oficiais.

## Exemplos de uso

### 1. Cadastrar e autenticar

```json
POST /api/auth/register
{
  "name": "Ana Silva",
  "email": "ana@example.com",
  "password": "senha123"
}
```

```json
POST /api/auth/login
{
  "email": "ana@example.com",
  "password": "senha123"
}
```

A resposta do login contém o `token`, sua validade e os dados públicos do usuário.

### 2. Criar uma ocorrência

Envie JSON para `POST /api/occurrences` com o token do cidadão:

```json
{
  "category": "EVENTO_NATURAL",
  "type": "ALAGAMENTO",
  "description": "Trecho da via com acúmulo de água",
  "perceivedRisk": "ALTO",
  "latitude": -24.005000,
  "longitude": -46.402000,
  "neighborhood": "Boqueirão",
  "address": "Avenida Exemplo, 100"
}
```

A nova ocorrência começa automaticamente com status `REGISTRADA`. Guarde o `id` retornado.

### 3. Enviar a foto da ocorrência

A criação e a foto são duas requisições porque uma usa JSON e a outra usa arquivo multipart.

No Postman:

1. Faça `POST /api/occurrences/{id}/image` usando o `id` retornado.
2. Na aba **Authorization**, escolha Bearer Token e informe o token do cidadão que criou a ocorrência.
3. Em **Body**, selecione **form-data**.
4. Crie a chave `file`, altere seu tipo de **Text** para **File** e escolha a imagem.
5. Envie a requisição.

São aceitos JPEG, PNG e WebP de até 5 MB. O servidor verifica o conteúdo real do arquivo, cria um nome seguro e salva a foto no diretório local. Um novo envio para a mesma ocorrência substitui a foto anterior.

A resposta da ocorrência passa a conter, por exemplo:

```json
{
  "id": 10,
  "imageUrl": "/api/occurrences/10/image"
}
```

Como a imagem é protegida por JWT, o React deverá buscá-la com `fetch`, incluindo `Authorization`, transformar a resposta em `Blob` e então criar uma URL local com `URL.createObjectURL(blob)`. Um `<img src="...">` direto não consegue acrescentar esse cabeçalho.

### 4. Alterar status como administrador

```json
PATCH /api/admin/occurrences/10/status
{
  "status": "EM_ANALISE"
}
```

### 5. Criar e ativar um alerta

```json
POST /api/admin/alerts
{
  "title": "Alerta de chuva intensa",
  "type": "CHUVA_INTENSA",
  "severity": "ALTA",
  "description": "Possibilidade de chuva intensa em Praia Grande",
  "startAt": "2026-08-21T15:00:00Z",
  "endAt": "2026-08-21T21:00:00Z"
}
```

O alerta é criado desativado. Ative-o com `PATCH /api/admin/alerts/{id}/activate`. Ele aparece para o cidadão somente quando está ativado e o horário atual está entre `startAt` e `endAt`.

## Valores aceitos

- Categorias: `EVENTO_NATURAL`, `INFRAESTRUTURA_URBANA`.
- Tipos naturais: `ALAGAMENTO`, `ENCHENTE`, `QUEDA_ARVORE`, `DESLIZAMENTO`, `VENTOS_FORTES`, `RESSACA_MARITIMA`, `OUTRO`.
- Tipos de infraestrutura: `BURACO_RUA`, `BUEIRO_ENTUPIDO`, `POSTE_DANIFICADO`, `SEMAFORO_COM_PROBLEMA`, `RUA_BLOQUEADA`, `FALTA_ILUMINACAO`, `OUTRO`.
- Risco percebido: `BAIXO`, `MEDIO`, `ALTO`.
- Status: `REGISTRADA`, `EM_ANALISE`, `EM_ATENDIMENTO`, `RESOLVIDA`, `NAO_CONFIRMADA`.
- Tipo de alerta: `CHUVA_INTENSA`, `ALAGAMENTO`, `VENTOS_FORTES`, `RESSACA_MARITIMA`, `DESLIZAMENTO`, `OUTRO`.
- Severidade de alerta: `BAIXA`, `MODERADA`, `ALTA`.

O risco informado é apenas a percepção do cidadão, não uma classificação técnica ou oficial.

## Erros e validações

A API retorna erros JSON consistentes para validação, autenticação, permissão, recurso não encontrado e regra de negócio. Exemplo:

```json
{
  "timestamp": "2026-08-21T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Existem campos invalidos",
  "path": "/api/occurrences",
  "fieldErrors": {
    "description": "A descricao e obrigatoria"
  }
}
```

## Estrutura do backend

```text
backend/src/main/java/br/com/cidadeemfoco/
├── config/       configuração de segurança e OpenAPI
├── controller/   endpoints REST
├── dto/          dados de entrada e saída
├── entity/       entidades JPA
├── enums/        valores controlados do domínio
├── exception/    erros e tratamento global
├── repository/   acesso ao MySQL
├── security/     autenticação JWT
└── service/      regras de negócio
```

As migrations ficam em `backend/src/main/resources/db/migration/`.

## Testes

Execute toda a suíte com:

```powershell
cd backend
.\mvnw.cmd test
```

Os testes cobrem domínio, validações, serviços, controllers, JWT, permissões administrativas, alertas e armazenamento de imagens.

## Limitações conhecidas do MVP

- As fotos ficam no disco da máquina que executa a aplicação; não há armazenamento em nuvem.
- Não há integração real com Prefeitura, Defesa Civil ou serviço meteorológico.
- Não há IA, previsão própria, SMS, IoT, rotas de evacuação, aplicativo nativo ou microserviços.
- Latitude e longitude são informadas pelo cliente; o mapa interativo será responsabilidade do frontend.
