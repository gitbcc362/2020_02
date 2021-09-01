# Simple Distributed Pub/Sub
Trabalho prático 2 de Sistemas Distribuídos [BCC362].

## Execução

Broker e Client compilado no java 16.0.2.

### Broker
Utilizando a entrada interativa de configurações: `java -jar Broker.jar`

### Client
Utilizando as configurações padrões: `java -jar Client.jar`

Ou utilizando os argumentos abaixo: `java -jar Client.jar -ip 192.168.1.10 -p 30100 -bip 192.168.1.5 -bp 30000 -nc 15 -na 3`

|Args|Descrição|Padrão|Exemplo|
|:---:|:---:|:---:|:---:|
|-ip|Endereço do Cliente|localhost|`-ip 10.0.0.8`|
|-p|Porta inicial do Cliente|23000|`-p 23000`|
|-bip|Endereço do Broker|localhost|`-bp 10.0.0.5`|
|-bp|Porta do Broker|22000|`-bp 22000`|
|-nc|Número de Clientes|5|`-nc 5`|
|-na|Número de acessos por Clientes|5|`-na 5`|

Qualquer argumento não usado irá ser usado o padrão.