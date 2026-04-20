# Smart Trash

Dashboard full stack para monitoramento de lixeiras inteligentes com dados mockados, autenticação fake e visual inspirado nos HTMLs fornecidos como referência de hierarquia e direção visual.

## Stack

- Backend: Java 21, Spring Boot, Maven
- Frontend: React, Vite, TypeScript, Tailwind CSS
- Gráficos: Recharts
- Testes: JUnit 5, Spring Boot Test, MockMvc, Vitest e Testing Library

## Como Rodar o Backend

Requisitos: JDK 21 e Maven no PATH.

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Como Rodar o Frontend

```bash
cd frontend
npm install
npm run dev
```

O Vite abre em `http://127.0.0.1:5173`.

## Como Rodar os Testes

Backend:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm install
npm test
```

## Estrutura de Pastas

```text
backend/
  src/main/java/com/smarttrash/
    config/
    controller/
    dto/
    exception/
    mock/
    model/
    repository/
    service/
  src/test/java/com/smarttrash/
frontend/
  src/components/
  src/pages/
  src/services/
  src/types/
docs/
```

## Endpoints

- `POST /auth/login`
- `GET /dashboard/summary`
- `GET /dashboard/history`
- `GET /dashboard/regions`
- `GET /sensors`
- `GET /sensors/{id}`
- `GET /sensors/{id}/history`
- `GET /sensors/locations`

## Decisões de Escopo

- Não há banco de dados; o backend usa `InMemorySensorRepository`.
- Não há ingestão real de sensores; os dados são gerados em `MockSensorData`.
- Não há comandos ou ações diretas em sensores.
- O backend calcula o status a partir de `fillLevelPercent`.
- A geolocalização é manual nos mocks, pois não vem do sensor.
- A tela de mapa é um placeholder visual, mas já consome as localizações mockadas.
- A autenticação é fake e retorna um token mockado sem persistência.

## Funcionalidades Futuras

- Persistência em banco de dados.
- Ingestão real de leituras de sensores.
- Autenticação real com usuários e autorização por perfil.
- Mapa interativo com provedor geográfico.
- Alertas operacionais e rotas de coleta.
- Painéis com filtros por período, região e status.

## Fluxo TDD Adotado

Para esta fase, os testes foram escritos antes das implementações correspondentes:

- Regra de classificação de status em `BinStatusClassifierTest`.
- Serviços de dashboard e sensores em `DashboardServiceTest` e `SensorServiceTest`.
- Coerência dos mocks em `MockSensorDataTest`.
- Endpoints com MockMvc em `AuthControllerTest`, `DashboardControllerTest` e `SensorControllerTest`.
- Fluxos integrados em `SmartTrashApiIntegrationTest`.
- Frontend com testes de serviço, badge de status, login e dashboard.

Uma funcionalidade só deve ser considerada pronta quando a suíte correspondente passar em um ambiente com JDK 21, Maven e dependências NPM instaladas.
