# twitterAndRabbitMQ

Responsável por realizar testes com o sistema rabbitmq já instalado e configurado. 
A inspiração para a criação deste projeto foi um trabalho realizado na disciplica de sistemas distribuídos.

# Configuração

### 0 - Dependências

- Antes de começar a configurar o projeto Gn-Pix é necessário realizar os seguintes passos:
  - Clone o projeto [rabbitmq-docker](https://github.com/GabrielSouzasbl/rabbitmq)
  - Siga os passos descritos
  - Verifique se as instancias estão rodando

### 1 - Requisitos para rodar na máquina local

- NodeJS 10.x ou superior;

### 2 - Configurando o projeto

```shell
$ npm install
```
### 2 - Iniciando o emiter

- Acesse a pasta onde ele se encontra.
```shell
$ cd twitterAndRabbitMQ/handlers
```
- Execute o script emitter.js    

> **_NOTA:_**  O arquivo emitter.js inicia o crawler de tweets na rede social twitter a partir dos parâmetros solicitados e após isso envia os textos para as filas parametrizadas do rabbitMQ.
   
#### Sobre os parâmetros:
- Tipo de envio:
  - crawler: Indica que o produtor enviará mensagens extraídas do Twitter.
  - normal: Enviará apenas uma mensagem designada no parâmetro a seguir.

- Nó Produtor:
  - 0: Localhost
  - 5672: Porta do cluster node.

- Nome da fila:
  - Para o tipo de envio -crawler-, este parâmetro NÃO deve ser expecificado
  - "fila1" para o tipo de envio -normal-.

- Usuários a serem extraídos:
  - "usuario1 usuario2": user_name do usuario do twitter a ser crawleado. **Apenas para tipo de envio -crawler-**
  - "texto da mensagem": Texto da mensagem a ser enviada. **Apenas para tipo de envio -normal-**

- Quantidade de tweets:
  - Para o tipo de envio -normal-, este parâmetro NÂO deve ser expecificado
  - "100" para o tipo de envio -crawler-.
    
    
#### Exemplos de emitter:

```shell
$ node emitter.js crawler 5672 "folha Estadao" 100
```

```shell
$ node handlers/emitter.js normal 0 nano "mesage test"
```

### 3 - Iniciando o receiver

- Acesse a pasta onde ele se encontra.
```shell
$ cd twitterAndRabbitMQ/handlers
```
- Execute o script receiver.js    

> **_NOTA:_**  O arquivo receiver.js inicia o consumo das mensagens publicadas pelo produtor na fila do rabbitMQ criada.

#### Sobre os parâmetros:
- Nó Consumidor:
  - 0: Localhost
  - 5673: Porta do cluster node.

- Filas a serem consumidas:
  - "fila1 fila2 fila3": Indica o nome das filas que o nó consumidor vai escutar e receber mensagens.


#### Exemplos de emitter:

```shell
$ node handlers/receiver.js 0 folha Estadao
```

```shell
$ node handlers/receiver.js 5672 folha 
```

#### Referências e boas práticas com RabbitMq

https://www.rabbitmq.com/
https://www.cloudamqp.com/blog/2017-12-29-part1-rabbitmq-best-practice.html
https://www.cloudamqp.com/blog/2018-01-08-part2-rabbitmq-best-practice-for-high-performance.html
http://throughaglass.io/technology/Enterprise-Messaging-with-RabbitMQ-and-AMQP.html
https://shivama205.medium.com/rabbitmq-best-practices-67a27ef72a57
http://throughaglass.io/technology/RabbitMQ-cluster-with-Docker-and-Docker-Compose.html
