# Simple Distributed Pub/Sub Replicated
Trabalho prático 3 de Sistemas Distribuídos [BCC362].

## Execução

Broker e Client compilado no java 16.0.2.

### Broker
Utilizando a entrada interativa de configurações: `java -jar Broker.jar`

Ou utilizando os argumentos abaixo: `java -jar Broker.jar -bp 30000 -p -sb localhost -sbp 30001`

|Args|Descrição|Exemplo|
|:---:|:---:|:---:|
|-bp|Porta do Broker|`-bp 30000`|
|-p|Identifica como primário|`-p`|
|-sb|Endereço do Broker Backup|`-sb 127.0.0.1`|
|-sbp|Porta do Broker Backup|`-sbp 30001`|

### Client
Utilizando as configurações padrões: `java -jar Client.jar`

Ou utilizando os argumentos abaixo: `java -jar Client.jar -ip 192.168.1.10 -p 30100 -bip 192.168.1.5 -bp 30000 -bbip 192.168.1.6 -bbp 30000 -nc 15 -na 3`

|Args|Descrição|Padrão|Exemplo|
|:---:|:---:|:---:|:---:|
|-ip|Endereço do Cliente|localhost|`-ip 10.0.0.8`|
|-p|Porta inicial do Cliente|30100|`-p 30100`|
|-bip|Endereço do Broker Primário|localhost|`-bp 10.0.0.5`|
|-bp|Porta do Broker Primário|30000|`-bp 30000`|
|-bbip|Endereço do Broker Backup|localhost|`-bbip 10.0.0.6`|
|-bbp|Porta do Broker Backup|30001|`-bbp 30001`|
|-nc|Número de Clientes|5|`-nc 5`|
|-na|Número de acessos por Clientes|3|`-na 3`|

Qualquer argumento não usado irá ser usado o padrão.