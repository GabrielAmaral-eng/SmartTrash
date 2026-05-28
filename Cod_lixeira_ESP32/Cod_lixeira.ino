#include <WiFi.h>
#include <time.h>

#define MQTT_MAX_PACKET_SIZE 512

#include <PubSubClient.h>

// ================================
// CONFIGURAÇÕES WIFI
// ================================

const char* ssid = "A36";
const char* password = "********";

// ================================
// CONFIGURAÇÕES MQTT
// ================================

const char* mqtt_broker = "23.20.111.10";
const int mqtt_port = 1883;

const char* topico_mqtt = "eseg/smarttrash";

// ================================
// IDENTIFICAÇÃO DO SENSOR
// ================================

const char* SENSOR_ID = "bin-001";

// Localização fixa
const float LATITUDE = -23.575864486519464;
const float LONGITUDE = -46.640605852153634;

// Bateria fixa
const int BATTERY_PERCENT = 87;

// ================================
// MQTT E REDE
// ================================

WiFiClient espClient;
PubSubClient client(espClient);

// ================================
// PINOS
// ================================

const int PINO_TRIG = 4;
const int PINO_ECHO = 2;
const int PINO_LED = 5;

// ================================
// CONFIGURAÇÃO DA LIXEIRA
// ================================

// Altura total da lixeira (cm)
const float ALTURA_LIXEIRA = 23.0;

// Distância mínima considerada cheia
const float DISTANCIA_MINIMA = 0.0;

// ================================
// ESTADO DO SISTEMA
// ================================

int contadorCheio = 0;
bool lixeiraCheia = false;

// ================================
// FUNÇÃO MEDIR DISTÂNCIA
// ================================

float medirDistancia() {

  digitalWrite(PINO_TRIG, LOW);
  delayMicroseconds(2);

  digitalWrite(PINO_TRIG, HIGH);
  delayMicroseconds(10);

  digitalWrite(PINO_TRIG, LOW);

  long duracao = pulseIn(PINO_ECHO, HIGH, 30000);

  // Timeout do sensor
  if (duracao == 0) {
    return -1;
  }

  float distancia = (duracao * 0.0343) / 2;

  return distancia;
}

// ================================
// FUNÇÃO OBTER TIMESTAMP ATUAL
// ================================

String obterTimestampAtual() {
  struct tm timeinfo;

  if (!getLocalTime(&timeinfo)) {
    Serial.println("Erro ao obter horário NTP");
    return "";
  }

  char timestamp[25];

  strftime(timestamp, sizeof(timestamp), "%Y-%m-%dT%H:%M:%SZ", &timeinfo);

  return String(timestamp);
}

// ================================
// RECONEXÃO MQTT
// ================================

void reconectarMQTT() {

  while (!client.connected()) {

    Serial.print("Tentando conexão MQTT...");

    String clientId = "ESP32Lixeira-";
    clientId += String(random(0xffff), HEX);

    if (client.connect(clientId.c_str())) {

      Serial.println("Conectado ao Broker MQTT!");

    } else {

      Serial.print("Falhou. Estado MQTT: ");
      Serial.println(client.state());

      delay(5000);
    }
  }
}

// ================================
// SETUP
// ================================

void setup() {

  Serial.begin(115200);

  randomSeed(micros());

  pinMode(PINO_TRIG, OUTPUT);
  pinMode(PINO_ECHO, INPUT);
  pinMode(PINO_LED, OUTPUT);

  // ================================
  // CONEXÃO WIFI
  // ================================

  WiFi.begin(ssid, password);

  Serial.print("Conectando ao WiFi");

  while (WiFi.status() != WL_CONNECTED) {

    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi conectado!");

  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  // ================================
  // SINCRONIZAÇÃO DO HORÁRIO NTP
  // ================================

  configTime(0, 0, "pool.ntp.org", "time.google.com");

  Serial.println("Sincronizando horário NTP...");

  struct tm timeinfo;

  while (!getLocalTime(&timeinfo)) {
    Serial.println("Aguardando sincronização...");
    delay(1000);
  }

  Serial.println("Horário sincronizado!");

  // ================================
  // CONFIGURA MQTT
  // ================================

  client.setServer(mqtt_broker, mqtt_port);
}

// ================================
// LOOP PRINCIPAL
// ================================

void loop() {

  // ================================
  // GARANTE CONEXÃO MQTT
  // ================================

  if (!client.connected()) {
    reconectarMQTT();
  }

  client.loop();

  // ================================
  // VETOR DAS 3 MEDIÇÕES
  // ================================

  float medidas[3];

  // ================================
  // REALIZA 3 MEDIÇÕES
  // ================================

  for (int j = 0; j < 3; j++) {

    float soma = 0;
    int leiturasValidas = 0;

    // Média interna de 5 leituras
    for (int i = 0; i < 5; i++) {

      float distancia = medirDistancia();

      if (distancia != -1 &&
          distancia > 2 &&
          distancia < ALTURA_LIXEIRA) {

        soma += distancia;
        leiturasValidas++;
      }

      delay(50);
    }

    // Se não houver leituras válidas
    if (leiturasValidas == 0) {

      medidas[j] = ALTURA_LIXEIRA;

    } else {

      medidas[j] = soma / leiturasValidas;
    }

    Serial.print("Medição ");
    Serial.print(j + 1);
    Serial.print(": ");
    Serial.print(medidas[j]);
    Serial.println(" cm");

    // Aguarda 10 segundos entre medições
    if (j < 2) {
      delay(10000);
    }
  }

  // ================================
  // ORDENA VALORES
  // ================================

  for (int i = 0; i < 2; i++) {

    for (int j = i + 1; j < 3; j++) {

      if (medidas[j] < medidas[i]) {

        float temp = medidas[i];
        medidas[i] = medidas[j];
        medidas[j] = temp;
      }
    }
  }

  // ================================
  // MEDIANA
  // ================================

  float mediana = medidas[1];

  // ================================
  // CÁLCULO DA PORCENTAGEM
  // ================================

  float porcentagem =
    ((ALTURA_LIXEIRA - mediana) /
    (ALTURA_LIXEIRA - DISTANCIA_MINIMA)) * 100.0;

  // Limites
  if (porcentagem < 0) porcentagem = 0;
  if (porcentagem > 100) porcentagem = 100;

  // ================================
  // DEBUG SERIAL
  // ================================

  Serial.println("================================");

  Serial.print("Mediana final: ");
  Serial.print(mediana);
  Serial.println(" cm");

  Serial.print("Porcentagem: ");
  Serial.print(porcentagem);
  Serial.println(" %");

  // ================================
  // LÓGICA DA LIXEIRA
  // ================================

  // Considera cheia acima de 80%
  if (porcentagem >= 80) {

    contadorCheio++;

  } else {

    contadorCheio = 0;
  }

  // Confirma cheia após 2 ciclos
  if (contadorCheio >= 2) {

    lixeiraCheia = true;
  }

  // Histerese
  // Só volta para livre abaixo de 60%
  if (porcentagem <= 60) {

    lixeiraCheia = false;
  }

  // LED
  digitalWrite(PINO_LED, lixeiraCheia);

  // ================================
  // TIMESTAMP ATUAL
  // ================================

  String timestamp = obterTimestampAtual();

  // ================================
  // CRIAÇÃO DO JSON
  // ================================

  String payload = "{";

  payload += "\"sensorId\":\"";
  payload += SENSOR_ID;
  payload += "\",";

  payload += "\"distanceCm\":";
  payload += String(mediana, 2);
  payload += ",";

  payload += "\"fillLevelPercent\":";
  payload += String(porcentagem, 2);
  payload += ",";

  payload += "\"binHeightCm\":";
  payload += String(ALTURA_LIXEIRA, 2);
  payload += ",";

  payload += "\"batteryPercent\":";
  payload += String(BATTERY_PERCENT);
  payload += ",";

  payload += "\"latitude\":";
  payload += String(LATITUDE, 15);
  payload += ",";

  payload += "\"longitude\":";
  payload += String(LONGITUDE, 15);
  payload += ",";

  payload += "\"timestamp\":\"";
  payload += timestamp;
  payload += "\"";

  payload += "}";

  // ================================
  // DEBUG JSON
  // ================================

  Serial.println("JSON enviado:");
  Serial.println(payload);

  // ================================
  // PUBLICAÇÃO MQTT
  // ================================

  client.publish(topico_mqtt, payload.c_str());

  Serial.println("MQTT publicado!");
  Serial.println("================================");
}
