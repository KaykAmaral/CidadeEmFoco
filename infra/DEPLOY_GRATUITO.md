# Publicação gratuita do Cidade em Foco

Arquitetura escolhida:

- frontend React: Cloudflare Pages;
- backend Spring Boot: Render Free Web Service;
- banco MySQL: Aiven Free;
- fotos: Cloudinary Free.

Este guia cobre principalmente o backend. O frontend pode ser publicado
separadamente pela equipe responsável.

## 1. Criar o MySQL no Aiven

1. Crie uma conta gratuita no Aiven, sem cadastrar cartão.
2. Crie um serviço **MySQL** no plano **Free**.
3. Aguarde o estado `Running`.
4. Na tela **Overview**, copie host, porta, usuário, senha e database.
5. Monte a URL no formato:

```text
jdbc:mysql://HOST:PORT/defaultdb?sslmode=require&serverTimezone=UTC
```

O Flyway criará e atualizará as tabelas automaticamente no primeiro início.
O plano gratuito possui 1 GB; acompanhe o consumo pelo painel do Aiven.

## 2. Criar o armazenamento no Cloudinary

1. Crie uma conta no plano Free.
2. No painel, copie `cloud name`, `API key` e `API secret`.
3. Não envie o `API secret` ao Git nem ao frontend.
4. O backend criará a pasta `cidade-em-foco/occurrences` durante os uploads.

As credenciais ficam somente nas variáveis protegidas do Render. O backend
continua validando JPEG, PNG e WebP, com limite de 5 MB e quatro fotos por
ocorrência.

## 3. Publicar o backend no Render

1. No Render, selecione **New > Web Service** e conecte o repositório GitHub.
2. Defina o **Root Directory** como `backend`.
3. Escolha o runtime **Docker** e o plano **Free**.
4. Defina **Health Check Path** como `/actuator/health`.
5. Copie as variáveis de `infra/render-backend.env.example` para o painel,
   substituindo todos os valores de exemplo.
6. Marque senha do banco, `JWT_SECRET` e credenciais do Cloudinary como secretas.
7. Inicie o deploy e acompanhe os logs do Flyway.

O Render fornece automaticamente `PORT`; o backend já está preparado para
usá-la. O sistema de arquivos do Render não será usado para as fotos.

## 4. Validar o backend

1. Abra `https://SEU-BACKEND.onrender.com/actuator/health` e confirme o estado `UP`.
2. Abra `https://SEU-BACKEND.onrender.com/swagger-ui.html`.
3. Cadastre um cidadão e faça login.
4. Crie uma ocorrência e envie uma foto.
5. Confirme a imagem no painel do Cloudinary.
6. Reinicie o backend e confirme que a foto continua acessível.

## 5. Conectar o frontend

1. Publique o frontend no Cloudflare Pages.
2. Configure `VITE_API_URL=https://SEU-BACKEND.onrender.com`.
3. No Render, configure `CORS_ALLOWED_ORIGINS` com a URL exata do Pages.
4. Faça um novo deploy do backend após alterar o CORS.

## Limitações gratuitas

- O Render pode adormecer o backend após 15 minutos sem acessos; a primeira
  requisição seguinte pode demorar cerca de um minuto.
- Enquanto o backend estiver adormecido, tarefas agendadas não executam. Elas
  voltam no próximo início da aplicação.
- O Aiven Free pode desligar bancos sem atividade contínua, enviando um aviso
  antes; o serviço pode ser religado pelo painel.
- Os serviços gratuitos possuem limites de armazenamento e tráfego. Se um
  limite for atingido, interrompa novos uploads em vez de migrar automaticamente
  para um plano pago.
