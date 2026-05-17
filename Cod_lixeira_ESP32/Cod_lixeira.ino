#include <WiFi.h>
#include <PubSubClient.h> // Biblioteca necessária para o protocolo MQTT

const char* ssid = "Galaxy S21"; //Nome do  Wi-fi
const char* password = "fypv2990";//Senha do Wi-fi

// --- Configurações do MQTT ---
const char* mqtt_broker = "broker.hivemq.com"; // Endereço do broker 
const int mqtt_port = 1883; //Porta do broker
const char* topico_distancia = "projeto/lixeira/distancia"; //
const char* topico_status = "projeto/lixeira/status"; //Envio do Status da lixeira(vázia ou cheia)

// Instâncias de rede e MQTT
WiFiClient espClient; //adaptador de internet para o MQTT usar.
PubSubClient client(espClient); //Conecta ao broker

//terminais utilizados 
const int PINO_TRIG = 4;
const int PINO_ECHO = 2;
const int PINO_LED = 5;

// --- Variáveis de Estado ---
int contadorCheio = 0;
bool lixeiraCheia = false;

float medirDistancia() {
  digitalWrite(PINO_TRIG, LOW);
  delayMicroseconds(2);
  
  digitalWrite(PINO_TRIG, HIGH);
  delayMicroseconds(10);
  
  digitalWrite(PINO_TRIG, LOW);
  
  long duracao = pulseIn(PINO_ECHO, HIGH);
  float distancia = (duracao * 0.0343) / 2;
  
  return distancia;
}

//Função para garantir a conexão MQTT
void reconectarMQTT() {
  // Entra em loop até que o ESP32 esteja conectado ao broker
  while (!client.connected()) {
    Serial.print("Tentando conexão MQTT...");
    
    // Cria um ID de cliente único para evitar conflitos no broker
    String clientId = "ESP32Lixeira-";
    clientId += String(random(0xffff), HEX);
    
    // Tenta conectar
    if (client.connect(clientId.c_str())) {
      Serial.println("Conectado ao Broker!");
    } else {
      Serial.print("Falhou, estado atual = ");
      Serial.print(client.state());
      Serial.println(" Tentando novamente em 5 segundos...");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200); //comunicação serial
  
  pinMode(PINO_TRIG, OUTPUT);
  pinMode(PINO_ECHO, INPUT);
  pinMode(PINO_LED, OUTPUT);
  
  // Inicia conexão Wi-Fi
  WiFi.begin(ssid, password);
  Serial.print("Conectando ao WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado!");
  
  // Configura o endereço e a porta do broker MQTT
  client.setServer(mqtt_broker, mqtt_port);
}

void loop() {
  // Garante que o MQTT permaneça conectado
  if (!client.connected()) {
    reconectarMQTT();
  }
  
  // Função crítica: processa o tráfego de entrada e saída do MQTT
  client.loop();

  float soma = 0;
  int leiturasValidas = 0;
  
  for(int i = 0; i < 5; i++) {
    float distancia = medirDistancia();
    if(distancia > 2 && distancia < 50) {
      soma += distancia;
      leiturasValidas++;
    }
    delay(50);
  }
  
  if(leiturasValidas == 0) {
    return;
  }
  
  float media = soma / leiturasValidas;
  
  Serial.print("Media: ");
  Serial.print(media);
  Serial.println(" cm");
  
  // Lógica de Estado
  if(media <= 10) {
    contadorCheio++;
  } else {
    contadorCheio = 0;
  }
  
  if(contadorCheio >= 5) {
    lixeiraCheia = true;
  }
  
  // Histerese:é o atraso ou retardo na resposta de um sistema quando submetido a uma força ou estímulo externo, 
  //fazendo com que ele retenha propriedades de seu estado anterior
  if(media >= 15) {
    lixeiraCheia = false;
  }
  
  digitalWrite(PINO_LED, lixeiraCheia);

  // Publicação de Dados no MQTT
  
  // 1. Converter o float (média) para um array de char (C-string), pois o MQTT trafega texto
  char msgDistancia[10];
  dtostrf(media, 1, 2, msgDistancia); 
  
  // Publica a distância numérica
  client.publish(topico_distancia, msgDistancia);
  
  // Publica o status legível da lixeira
  if (lixeiraCheia) {
    client.publish(topico_status, "CHEIA");
  } else {
    client.publish(topico_status, "LIVRE");
  }

  delay(1000);
}