# Smart Trash

Dashboard full stack para monitoramento de lixeiras inteligentes. A fase atual usa Supabase para autenticacao, banco de dados e persistencia operacional. O frontend autentica no Supabase, envia o JWT do usuario autenticado para o backend Spring Boot, e o backend consulta o Supabase respeitando RLS.

## Stack

- Backend: Java 21, Spring Boot, Maven
- Frontend: React, Vite, TypeScript, Tailwind CSS
- Banco/Auth: Supabase Auth + Postgres com RLS
- Graficos: Recharts
- Mapa: Leaflet com OpenStreetMap
- Testes: JUnit 5, Spring Boot Test, MockMvc, Vitest e Testing Library

## Supabase

O projeto Supabase conectado e `Smart Trash` (`bpfqpfounhbbrhbikqxw`). As migrations locais ficam em `supabase/migrations/` e criam:

- `profiles`: perfil ligado a `auth.users`, criado automaticamente por trigger.
- `smart_bins`: cadastro e estado atual das lixeiras.
- `sensor_readings`: historico de leituras por sensor.
- `collection_assignments`: alocacoes de equipes de coleta.

Todas as tabelas publicas estao com RLS ativo. As tabelas operacionais podem ser lidas apenas por usuarios autenticados, e novas coletas so podem ser criadas pelo proprio usuario autenticado.

Roles do sistema:

- `SUPER_ADMIN`: acesso total e configuracao de usuarios. Inicialmente reservado para `gabriel_41231@aluno.eseg.edu.br`.
- `ADMIN`: acesso total as funcionalidades operacionais.
- `OPERATOR`: visualizacao de sensores e alocacao de equipes.
- `VIEWER`: acesso apenas ao dashboard e mapa.

## Variaveis do Frontend

Copie `frontend/.env.example` para `frontend/.env.local` se precisar trocar o projeto Supabase:

```bash
VITE_SUPABASE_URL=https://bpfqpfounhbbrhbikqxw.supabase.co
VITE_SUPABASE_PUBLISHABLE_KEY=sb_publishable_af7Dllj2Q5z0vYvsVPuxug_oV8Dcej7
VITE_API_BASE_URL=http://localhost:8080
```

As telas sempre consomem o backend. O frontend envia `Authorization: Bearer <access_token>` usando a sessao atual do Supabase.

## Variaveis do Backend

O backend usa Supabase como fonte unica dos dados operacionais. Configure:

```bash
SMARTTRASH_DATA_SOURCE=supabase
SUPABASE_URL=https://bpfqpfounhbbrhbikqxw.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_af7Dllj2Q5z0vYvsVPuxug_oV8Dcej7
```

O backend usa a publishable key apenas como `apikey` e repassa o token do usuario no header `Authorization`; assim as queries executam como `authenticated` e passam pelas policies RLS.

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
npm test
```

## Endpoints do Backend

- `GET /auth/profile`
- `GET /auth/users`
- `PATCH /auth/users/{userId}/role`
- `GET /health`
- `GET /dashboard/summary`
- `GET /dashboard/history`
- `GET /dashboard/regions`
- `GET /sensors`
- `GET /sensors/{id}`
- `GET /sensors/{id}/history`
- `GET /sensors/locations`
- `GET /collections`
- `GET /collections/scheduled-route`
- `POST /collections/allocations/{sensorId}`

## Decisoes de Escopo

- O login funcional do frontend usa Supabase Auth com email/senha.
- Dashboard, sensores, mapa, perfil e coletas passam pelo backend.
- O backend consulta `profiles`, `smart_bins`, `sensor_readings` e `collection_assignments` com o JWT recebido e RLS.
- A alocacao de equipe persiste em `collection_assignments` para lixeiras com mais de 70% de enchimento.
- A rota programada diaria sai as 12h e inclui apenas lixeiras acima de 50% de enchimento. Se nao houver pontos elegiveis, a rota fica sem recolhimento.
- As lixeiras foram posicionadas na regiao do Paraiso, entre a Faculdade ESEG e o Colegio Etapa.
- A ingestao real de sensores ainda nao foi implementada; as migrations semeiam os dados iniciais no Supabase.
