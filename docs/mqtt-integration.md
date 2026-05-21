# Integração MQTT

O backend do SmartTrash assina um tópico MQTT ao iniciar a aplicação e transforma cada payload recebido em uma leitura da lixeira cadastrada.

## Configuração

As variáveis abaixo controlam a conexão com o broker:

| Variável | Propriedade Spring | Padrão |
| --- | --- | --- |
| `MQTT_ENABLED` | `mqtt.enabled` | `true` |
| `MQTT_BROKER_URL` | `mqtt.broker-url` | `tcp://localhost:1883` |
| `MQTT_CLIENT_ID` | `mqtt.client-id` | `smarttrash-backend` |
| `MQTT_TOPIC` | `mqtt.topic` | `eseg/smarttrash` |

Para gravar no Supabase a partir do listener MQTT, o backend também precisa de uma chave de serviço no ambiente:

```bash
SUPABASE_SECRET_KEY=...
```

`SUPABASE_SERVICE_ROLE_KEY` também é aceito como alternativa. Essa chave fica apenas no backend.

## Tópico

```text
eseg/smarttrash
```

## JSON esperado

Campos obrigatórios:

| Campo | Tipo | Significado |
| --- | --- | --- |
| `sensorId` | string | ID da lixeira já cadastrada em `smart_bins.id`, por exemplo `bin-001`. |
| `distanceCm` | number | Distância medida pelo sensor em centímetros. Deve ser maior ou igual a `0`. |
| `fillLevelPercent` | number | Nível de preenchimento de `0` a `100`. |
| `timestamp` | string ISO-8601 | Data/hora da leitura. Use UTC com `Z` quando possível. |

Campos opcionais:

| Campo | Tipo | Significado |
| --- | --- | --- |
| `binHeightCm` | number | Altura da lixeira em centímetros. Se enviado, atualiza `smart_bins.bin_height_cm`. |
| `latitude` | number | Latitude da lixeira. Se enviada, atualiza `smart_bins.latitude`. |
| `longitude` | number | Longitude da lixeira. Se enviada, atualiza `smart_bins.longitude`. |
| `batteryPercent` | integer | Bateria de `0` a `100`. O backend valida e aceita o campo, mas o schema atual não possui coluna para persisti-lo. |

Exemplo válido:

```json
{
  "sensorId": "bin-001",
  "distanceCm": 42.0,
  "fillLevelPercent": 65.0,
  "binHeightCm": 120.0,
  "batteryPercent": 87,
  "latitude": -23.5749,
  "longitude": -46.6407,
  "timestamp": "2026-05-13T20:30:00Z"
}
```

## Processamento

Para cada mensagem válida, o backend:

1. Valida o JSON e os campos obrigatórios.
2. Confere se `sensorId` já existe em `smart_bins`.
3. Calcula o status usando a regra atual do backend:
   - `0` até `49.99`: `EMPTY`
   - `50` até `79.99`: `ATTENTION`
   - `80` até `100`: `FULL`
4. Faz upsert em `sensor_readings` usando `(sensor_id, recorded_at)`.
5. Atualiza `smart_bins.current_distance_cm`, `current_fill_level_percent`, `status` e `last_update`.

Mensagens inválidas são logadas e descartadas sem derrubar a aplicação.

## Teste com mosquitto_pub

Broker local:

```bash
mosquitto_pub -h localhost -p 1883 -t eseg/smarttrash -m '{"sensorId":"bin-001","distanceCm":42.0,"fillLevelPercent":65.0,"binHeightCm":120.0,"batteryPercent":87,"timestamp":"2026-05-13T20:30:00Z"}'
```

Broker em EC2:

```bash
MQTT_BROKER_URL=tcp://IP_PUBLICO_DA_EC2:1883
mosquitto_pub -h IP_PUBLICO_DA_EC2 -p 1883 -t eseg/smarttrash -m '{"sensorId":"bin-001","distanceCm":42.0,"fillLevelPercent":65.0,"binHeightCm":120.0,"batteryPercent":87,"timestamp":"2026-05-13T20:30:00Z"}'
```

## Teste com curl

Não há endpoint HTTP específico para simular MQTT. Para verificar o resultado pela API atual, consulte o sensor depois de publicar:

```bash
curl http://localhost:8080/sensors/bin-001
```

Se o backend estiver usando Supabase em produção, essa rota pode exigir o mesmo token de usuário que o frontend envia.

## Logs

Sucesso:

```text
Processed MQTT reading for sensor bin-001 at 2026-05-13T20:30:00Z: distanceCm=42.0, fillLevelPercent=65.0, status=ATTENTION
```

Payload inválido:

```text
Invalid MQTT payload received: ...
```

Sensor não cadastrado:

```text
Rejected MQTT payload because sensor missing is not registered
```
