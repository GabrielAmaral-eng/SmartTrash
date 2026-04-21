# Arquitetura do Smart Trash

## Visão Geral

O sistema monitora lixeiras inteligentes com dados mockados. A API expõe dashboard, listagem de sensores, detalhe, histórico, localização geográfica e alocação mockada de equipes de coleta para lixeiras acima do limite operacional.

## Backend

Camadas:

- `controller`: expõe os endpoints REST.
- `service`: concentra regras de negócio e agregações.
- `repository`: abstrai a origem dos dados mockados.
- `mock`: gera sensores, histórico e localização manual.
- `model`: representa domínio interno.
- `dto`: define respostas e entradas da API.
- `exception` e `config`: tratamento de erros e CORS.

Regra de coleta:

- `POST /collections/allocations/{sensorId}` cria ou retorna uma alocação existente.
- Apenas lixeiras com `fillLevelPercent > 70` podem receber equipe de coleta.
- A alocação é armazenada em memória e retorna status, horário de saída, previsão de coleta, equipe responsável e progresso.
- `GET /collections` lista as coletas ativas e inclui um registro mockado inicial para demonstração.

Regra de status:

- `0..49%`: `EMPTY`
- `50..79%`: `ATTENTION`
- `80..100%`: `FULL`

## Frontend

Camadas:

- `services/api.ts`: comunicação com backend.
- `types/api.ts`: contratos TypeScript equivalentes aos DTOs.
- `components/ui`: cards, painéis e status.
- `components/charts`: gráficos Recharts.
- `components/layout`: layout compartilhado com sidebar e topbar.
- `pages`: telas de login, dashboard, sensores, coleta e mapa.

Mapa:

- A tela de mapa usa `react-leaflet` e `leaflet`.
- Os tiles são carregados do OpenStreetMap.
- Os sensores são renderizados como marcadores circulares coloridos por status.

## Observação de Ambiente

O projeto foi configurado para Java 21 conforme requisito. Se o ambiente local tiver apenas Java 17 ou não tiver Maven, os testes do backend não conseguirão rodar até a instalação desses requisitos.
