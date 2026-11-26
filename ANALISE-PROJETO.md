# Análise do Cidade em Foco

Revisão realizada em 28/09/2026, com leitura da estrutura, documentação, configuração, fluxos principais do frontend, serviços, segurança, entidades, migrations e testes do backend. Não equivale a uma auditoria de segurança nem a uma validação visual de todas as telas.

## Arquitetura e funcionalidades

- Frontend: React 19, Vite 8, React Router, CSS próprio, Leaflet e Lucide. Páginas, componentes, serviços HTTP, contexto de sessão e rotas por perfil estão separados.
- Backend: Java com alvo 21, Spring Boot 4.1.1, Spring Security, JPA, Bean Validation, JWT e Swagger. Organização em controllers, DTOs, serviços, repositórios e entidades.
- Persistência: MySQL, com três migrations Flyway para usuários, ocorrências, tipos e imagens. Hibernate valida o esquema; não o recria automaticamente.
- Cidadão: cadastro, login, painel, mapa, filtros, minhas ocorrências, detalhes, registro com localização e até quatro imagens, perfil e alertas ativos.
- Administrador: painel, consulta de ocorrências, mudança de status e criação/ativação/desativação de alertas.
- Mapas e endereços dependem de OpenStreetMap/Nominatim. Alertas são cadastrados manualmente e demonstrativos.

## Pontos positivos

- Cadastro fixa o perfil CITIZEN; permissões administrativas são verificadas pela API.
- Senhas usam BCrypt; tokens são assinados e verificados; autenticação consulta o usuário no banco.
- Upload verifica autoria, quantidade, tamanho e assinatura inicial do formato; nomes de arquivos são gerados pelo servidor e caminhos são normalizados.
- Erros de validação têm resposta estruturada. Categoria/tipo, coordenadas e período de alertas possuem validações.
- Frontend trata expiração de sessão e respostas 401 nos fluxos principais; imagens protegidas são carregadas como Blob.
- Criação da ocorrência e upload são separados, com aviso quando apenas as imagens falham.

## Achados e prioridades

1. **Upload múltiplo incompatível com o limite total da requisição.** `ImagePicker.jsx` aceita quatro imagens de até 5 MB cada e `occurrenceService.js` envia todas juntas. `application.yml` limita a requisição inteira a 5 MB. Arquivos individualmente válidos podem, juntos, ser rejeitados. Ajustar o limite total, incluindo a margem do multipart, e testar a integração HTTP.
2. **Documentação do backend desatualizada.** O README descreve uma foto substituível, enquanto `OccurrenceImageService` adiciona até quatro e há endpoint `/images`. Também faltam tipos adicionados na migration V2. Atualizar exemplos e comportamento documentado.
3. **Listagens sem paginação.** Serviços de ocorrências e alertas retornam listas completas. O carregamento das imagens da coleção JPA durante a conversão de cada ocorrência também merece medição de consultas adicionais. Introduzir paginação e verificar consultas SQL antes de ampliar o volume de dados.
4. **Possível corrida na seleção de localização.** `NewOccurrencePage.updatePoint` aplica o resultado da geocodificação reversa sem verificar se ainda corresponde ao último ponto escolhido. Cliques rápidos podem fazer uma resposta antiga sobrescrever a localização recente. Usar cancelamento ou identificação da última solicitação.
5. **Possível corrida no limite de fotos.** O serviço verifica a quantidade e adiciona imagens sem bloqueio explícito ou versão otimista na ocorrência. Uploads simultâneos merecem teste para garantir o limite de quatro.
6. **Bundle inicial grande.** O build gerou JavaScript de aproximadamente 667 KB, 200 KB gzip, com aviso do Vite. As páginas são importadas diretamente em `App.jsx`; separar rotas e mapas sob demanda pode reduzir o carregamento inicial.
7. **Cobertura do frontend ausente.** Não existe script de testes do frontend. A suíte Java usa mocks em vários cenários; sua aprovação não comprova todos os fluxos contra MySQL e navegador reais.

## Verificações executadas

- `npm ci`: dependências do lockfile instaladas.
- `npm run lint`: aprovado.
- `npm run build`: aprovado, com aviso de tamanho do bundle.
- Maven `test`: 66 testes, zero falhas, zero erros, zero ignorados.
- Ambiente usado: Node 24.14.0 e JDK 24.0.1; compilação Java configurada para release 21. Não foi executada uma segunda rodada com JDK 21.

## Visualização local

- Frontend configurado por `frontend/.env.local`, apontando para `http://localhost:8080`.
- Interface: `http://127.0.0.1:5173`.
- API: `http://127.0.0.1:8080`; documentação em `/swagger-ui.html`.
- Banco separado em `backend/target/preview-mysql`, porta 3307, ligado apenas a 127.0.0.1. Dados de visualização começam vazios; a pasta `target` pode ser removida por um Maven clean, portanto não deve armazenar dados importantes.
- Logs de inicialização: `frontend/preview.out.log`, `frontend/preview.err.log`, `backend/target/preview-api.out.log` e `backend/target/preview-api.err.log`.

Nenhuma alteração nas regras de negócio foi feita nesta análise.
