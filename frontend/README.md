# Cidade em Foco — Frontend

Frontend do MVP Cidade em Foco, construído com React, JavaScript e Vite.

## Requisitos

- Node.js compatível com o Vite
- Backend executando em `http://localhost:8080`

## Executar

```powershell
npm.cmd install
npm.cmd run dev
```

A aplicação abre por padrão em `http://localhost:5173`.

## Variável de ambiente

Copie `.env.example` para `.env.local` caso precise alterar a URL da API:

```env
VITE_API_URL=http://localhost:8080
```

## Verificações

```powershell
npm.cmd run lint
npm.cmd run build
```

## Organização visual

- `src/components/ui`: componentes visuais reutilizáveis;
- `src/constants`: textos e configurações compartilhadas;
- `src/styles/ui.css`: estilos dos componentes básicos;
- `src/index.css`: cores, tipografia e estilos globais.

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
