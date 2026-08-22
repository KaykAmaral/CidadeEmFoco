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
| `/app/alertas` | Alertas para o cidadão |
| `/app/perfil` | Perfil do cidadão |
| `/admin` | Visão geral administrativa |
| `/admin/ocorrencias` | Gestão de ocorrências |
| `/admin/alertas` | Gestão de alertas |

As rotas privadas ainda exibem somente conteúdo estrutural. A proteção por
perfil será aplicada junto da autenticação, antes da integração com dados reais.
