# Arquitetura do Smart Trash

## Visao Geral

O sistema monitora lixeiras inteligentes com dados mockados ou dados Supabase. A API expoe dashboard, listagem de sensores, detalhe, historico, localizacao geografica e alocacao de equipes de coleta para lixeiras acima do limite operacional.

## Backend

Camadas:

- `controller`: expoe os endpoints REST.
- `service`: concentra regras de negocio e agregacoes.
- `repository`: abstrai a origem dos dados. O modo padrao usa memoria; `SMARTTRASH_DATA_SOURCE=supabase` ativa repositorios PostgREST.
- `mock`: gera sensores, historico e localizacao manual.
- `model`: representa dominio interno.
- `dto`: define respostas e entradas da API.
- `exception` e `config`: tratamento de erros e CORS.

Regra de coleta:

- `POST /collections/allocations/{sensorId}` cria ou retorna uma alocacao existente.
- Apenas lixeiras com `fillLevelPercent > 70` podem receber equipe de coleta.
- A alocacao retorna status, horario de saida, previsao de coleta, equipe responsavel e progresso.
- `GET /collections` lista as coletas ativas. No modo memoria inclui um registro mockado inicial para demonstracao.

Supabase/RLS:

- O frontend autentica com Supabase Auth e envia o `access_token` ao backend em `Authorization: Bearer <token>`.
- O backend usa a publishable key como `apikey` e o JWT recebido como `Authorization`, fazendo as consultas ao PostgREST como o usuario autenticado.
- Sem Bearer token, os repositorios Supabase retornam `401`, evitando consultas anonimas acidentais contra tabelas protegidas por RLS.
- A role `SUPER_ADMIN` e aplicada apenas ao email `gabriel_41231@aluno.eseg.edu.br` nas migrations.
- A tela `/usuarios` lista e altera roles somente para `SUPER_ADMIN`.

Regra de status:

- `0..49%`: `EMPTY`
- `50..79%`: `ATTENTION`
- `80..100%`: `FULL`

Regras de acesso:

- `SUPER_ADMIN`: acesso total e configuracao de usuarios.
- `ADMIN`: acesso total as funcionalidades operacionais.
- `OPERATOR`: sensores, coleta manual, dashboard e mapa.
- `VIEWER`: dashboard e mapa.

Rotas:

- Manual: operadores, admins e super-admins podem alocar equipe em lixeiras com `fillLevelPercent > 70`.
- Programada: todos os dias as 12h, a rota inclui somente lixeiras com `fillLevelPercent > 50`; se nao houver nenhuma, nao ha recolhimento.

## Frontend

Camadas:

- `services/api.ts`: comunicacao com o backend e envio do JWT da sessao Supabase.
- `types/api.ts`: contratos TypeScript equivalentes aos DTOs.
- `components/ui`: cards, paineis e status.
- `components/charts`: graficos Recharts.
- `components/layout`: layout compartilhado com sidebar e topbar.
- `pages`: telas de login, dashboard, sensores, coleta e mapa.

Mapa:

- A tela de mapa usa `react-leaflet` e `leaflet`.
- Os tiles sao carregados do OpenStreetMap.
- Os sensores sao renderizados como marcadores circulares coloridos por status.
- A visualizacao foca a regiao do Paraiso, aproximando Faculdade ESEG e Colegio Etapa, e desenha a rota programada quando ha paradas elegiveis.

## Observacao de Ambiente

O projeto foi configurado para Java 21 conforme requisito. Se o ambiente local tiver apenas Java 17 ou nao tiver Maven, os testes do backend nao conseguirao rodar ate a instalacao desses requisitos.
