# API pública do Cidade em Foco

## Escopo e versionamento

A superfície destinada a integrações externas usa o prefixo `/api/v1/public`. Mudanças incompatíveis deverão usar um novo prefixo, como `/api/v2/public`; a versão `v1` não deve ter campos removidos ou semântica alterada sem período de migração.

## Endpoints públicos

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/v1/public/alerts/active` | Alertas climáticos ativos |
| `GET` | `/api/v1/public/occurrences?page=0&size=20` | Resumos paginados de ocorrências |
| `GET` | `/actuator/health` | Estado básico do serviço |
| `GET` | `/v3/api-docs/public-v1` | Documento OpenAPI público |
| `GET` | `/swagger-ui.html` | Interface Swagger UI |

Os resumos públicos de ocorrências não incluem nome, e-mail, telefone, descrição livre, endereço, coordenadas, imagens ou identificadores do usuário. Os endpoints `/api/admin/**`, `/api/users/me/**`, `/api/occurrences/**` e demais rotas internas continuam autenticados.

## Inventário das rotas existentes

Públicas:

- `POST /api/auth/register` e `POST /api/auth/login`;
- `GET /api/v1/public/alerts/active` e `GET /api/v1/public/occurrences`;
- `GET /actuator/health`, `/v3/api-docs/**` e `/swagger-ui/**`.

Autenticadas para cidadãos ou usuários da aplicação:

- `GET /api/alerts/active` e `GET /api/alerts/stream`;
- `GET /api/occurrences`, `/api/occurrences/map`, `/api/occurrences/{id}` e imagens;
- `POST /api/occurrences` e uploads de imagens, restritos a cidadão;
- `GET /api/occurrences/mine`, restrito a cidadão;
- `GET`, `PUT` e `DELETE /api/users/me/whatsapp`, restritos ao próprio cidadão.

Administrativas, sempre com JWT e função `ADMIN`:

- `/api/admin/alerts/**`;
- `/api/admin/occurrences/**`;
- `/api/admin/whatsapp-notifications/**`.

As rotas autenticadas antigas não fazem parte do contrato público versionado e podem retornar dados detalhados necessários ao aplicativo. Aplicações externas devem usar somente `/api/v1/public/**`.

## Paginação e erros

`page` começa em zero. `size` aceita valores de 1 a 100. Respostas paginadas incluem `content`, `page`, `size`, `totalElements`, `totalPages`, `first` e `last`.

Erros usam o mesmo objeto JSON com `timestamp`, `status`, `error`, `message`, `path` e `fieldErrors`. Exemplos usam somente dados fictícios.

## Rate limiting

O limite padrão é de 60 requisições por minuto por endereço de origem para `/api/v1/public/**`. As respostas incluem `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`. Ao exceder o limite, a API responde `429` e `Retry-After`.

Configuração:

```dotenv
PUBLIC_API_RATE_LIMIT_ENABLED=true
PUBLIC_API_RATE_LIMIT_PER_MINUTE=60
```

Em produção com múltiplas instâncias, substitua o contador em memória por um armazenamento compartilhado antes de considerar o limite global. A resolução do IP do cliente pelo proxy também deve ser aprovada e configurada na infraestrutura; a aplicação não confia diretamente em `X-Forwarded-For` fornecido pelo cliente.

## CORS e autenticação

Defina `CORS_ALLOWED_ORIGINS` como uma lista separada por vírgulas de origens HTTPS autorizadas. Não use `*` com endpoints autenticados. A API pública v1 aceita leitura sem JWT; rotas de cidadão usam JWT e rotas administrativas exigem a função `ADMIN`.

## Publicação

1. Configure banco, `JWT_SECRET`, origens CORS e limites no ambiente de homologação.
2. Execute `mvn test` no backend e `npm run lint && npm run build` no frontend.
3. Valide `/actuator/health` e `/v3/api-docs/public-v1`.
4. Faça testes de autorização confirmando `401`/`403` nas rotas internas e administrativas.
5. Faça teste de carga compatível com a capacidade prevista e valide respostas `429`.
6. Revise retenção, termos de uso e política de privacidade.
7. Somente após aprovação explícita, publique a mesma imagem validada em produção.

O preparo deste repositório não executa deploy nem altera o ambiente de produção.
