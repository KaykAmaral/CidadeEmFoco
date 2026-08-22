# Cidade em Foco — Frontend

Frontend do MVP Cidade em Foco, construído com React, JavaScript e Vite.

## Requisitos

- Node.js compatível com o Vite
- Backend executando em `http://localhost:8080`

## Executar

```powershell
npm.cmd --prefix frontend install
npm.cmd --prefix frontend run dev
```

A aplicação abre por padrão em `http://localhost:5173`.

## Variável de ambiente

Copie `frontend/.env.example` para `frontend/.env.local` caso precise alterar a URL da API:

```env
VITE_API_URL=http://localhost:8080
```

## Verificações

```powershell
npm.cmd --prefix frontend run lint
npm.cmd --prefix frontend run build
```

## Organização visual

- `frontend/src/components/ui`: componentes visuais reutilizáveis;
- `frontend/src/constants`: textos e configurações compartilhadas;
- `frontend/src/styles/ui.css`: estilos dos componentes básicos;
- `frontend/src/index.css`: cores, tipografia e estilos globais.

Os componentes visuais não acessam a API e não possuem regras de negócio. Eles
serão reutilizados nas páginas das próximas etapas.

## Rotas atuais

| Rota | Área |
| --- | --- |
| `/login` | Login público |
| `/cadastro` | Cadastro de cidadão |
| `/app` | Início do cidadão |
| `/app/ocorrencias` | Ocorrências para o cidadão |
| `/app/ocorrencias/nova` | Registro de ocorrência |
| `/app/ocorrencias/:id` | Detalhes da ocorrência |
| `/app/alertas` | Alertas para o cidadão |
| `/app/perfil` | Perfil do cidadão |
| `/admin` | Visão geral administrativa |
| `/admin/ocorrencias` | Gestão de ocorrências |
| `/admin/alertas` | Gestão de alertas |

As rotas `/app/*` exigem um usuário `CITIZEN` e as rotas `/admin/*` exigem um
usuário `ADMIN`. Usuários sem sessão são redirecionados para `/login`.

## Autenticação

O login utiliza `POST /api/auth/login` e o cadastro utiliza
`POST /api/auth/register`. O cadastro público não permite escolher perfil e
sempre cria um cidadão, conforme a regra do backend.

Para manter a sessão após atualizar a página, o JWT e os dados públicos do
usuário ficam no `localStorage` até o logout ou o vencimento do token. A senha
nunca é armazenada pelo frontend.

## Dashboard do cidadão

A rota `/app` consulta em paralelo:

- `GET /api/alerts/active` para o alerta climático atual;
- `GET /api/occurrences` para o mapa e as ocorrências recentes.

O mapa utiliza Leaflet com os tiles públicos do OpenStreetMap e exibe a
atribuição obrigatória. Ele não usa geocodificação, não envia ocorrências para
serviços externos e não oferece download de mapas para uso offline.

## Ocorrências e fotos

A listagem permite consultar todas as ocorrências, aplicar os filtros aceitos
pelo backend e alternar para os registros do usuário autenticado.

O cadastro da ocorrência e o envio da foto são duas requisições:

1. `POST /api/occurrences` cria a ocorrência e devolve seu ID;
2. se uma foto foi selecionada, `POST /api/occurrences/{id}/image` envia o
   arquivo como `multipart/form-data`.

A foto é opcional e deve ser JPEG, PNG ou WebP com até 5 MB. Se a ocorrência for
criada e somente o envio da foto falhar, o registro é preservado e o frontend
mostra um aviso. Como a consulta da imagem exige autenticação, o frontend baixa
o arquivo com o JWT e cria uma URL temporária apenas para exibi-lo.

## Alertas para o cidadão

A rota `/app/alertas` utiliza `GET /api/alerts/active` e apresenta somente os
alertas ativos e dentro do período de validade retornados pelo backend. Cada
alerta mostra tipo, severidade, descrição, início, fim e o aviso demonstrativo.

O frontend não consulta serviços meteorológicos e não produz previsão própria.

## Perfil do cidadão

A rota `/app/perfil` apresenta os dados públicos devolvidos no login: nome,
e-mail, perfil de acesso e data de cadastro. Ela também consulta
`GET /api/occurrences/mine` para mostrar as quantidades e os registros recentes
do próprio cidadão.

O MVP não possui endpoints para editar o perfil ou alterar a senha. Por isso,
essas ações não são exibidas pelo frontend.

## Dashboard administrativo

A rota `/admin` consulta `GET /api/admin/occurrences` e
`GET /api/admin/alerts`. A partir dessas respostas, o frontend apresenta:

- total de ocorrências e contagens por status;
- mapa com os registros cadastrados;
- ocorrências mais recentes;
- distribuição visual dos cinco status oficiais;
- alertas recentes e quantidade marcada como ativa.

O dashboard é somente para consulta. Atualizações de status e gerenciamento de
alertas permanecem nas telas administrativas específicas.

## Administração de alertas

A rota `/admin/alertas`, exclusiva para `ADMIN`, permite:

- consultar todos os alertas cadastrados;
- criar um alerta demonstrativo, inicialmente inativo;
- ativar ou desativar um alerta.

O formulário envia as datas em formato ISO para `POST /api/admin/alerts`. As
ações utilizam `PATCH /api/admin/alerts/{id}/activate` e
`PATCH /api/admin/alerts/{id}/deactivate`. A ativação não altera o período de
validade informado no cadastro.

## Administração de ocorrências

A rota `/admin/ocorrencias` permite filtrar e consultar os registros usando
`GET /api/admin/occurrences`. Cada resultado abre a rota
`/admin/ocorrencias/:id`, que mostra descrição, risco percebido, datas,
localização e foto.

Somente nessa área o status pode ser alterado. A atualização envia:

```text
PATCH /api/admin/occurrences/{id}/status
```

O valor deve ser um dos cinco status oficiais do backend.

## Checklist de validação manual

Com o backend e o frontend em execução, valide no navegador:

1. cadastro de uma conta cidadã e login;
2. acesso às rotas protegidas e encerramento da sessão;
3. registro de ocorrência com e sem foto;
4. consulta do mapa, filtros, detalhes e próprias ocorrências;
5. consulta dos alertas ativos pelo cidadão;
6. login como administrador;
7. filtros administrativos e atualização de status;
8. criação, ativação e desativação de alerta;
9. navegação em largura de celular e de computador;
10. mensagens de validação, carregamento, lista vazia e erro de conexão.

O frontend não possui uma suíte automatizada própria nesta versão. As
verificações disponíveis são `npm.cmd --prefix frontend run lint` e
`npm.cmd --prefix frontend run build`; as regras
e permissões da API são cobertas pelos testes automatizados do backend.
