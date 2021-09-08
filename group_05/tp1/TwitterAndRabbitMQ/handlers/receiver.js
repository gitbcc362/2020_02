const amqplib = require('amqplib');

const exchangeName = 'direct_tweets';

let args = process.argv.slice(2);

const messenger = {
  port: args[0],
  routingKey: args.slice(1)
};

console.log(messenger);

const receiveMsg = async () => {
  const url = messenger.port && messenger.port.length ===  4 ? connect(messenger.port) : localhost();

  const connection = await amqplib.connect(url);
  const channel = await connection.createChannel();

  await channel.assertExchange(exchangeName, 'direct', { durable: true });

  const q = await channel.assertQueue('', { exclusive: true });

  messenger.routingKey.forEach(function (severity) {
    channel.bindQueue(q.queue, exchangeName, severity);
  });

  console.log('listening...');

  channel.consume(q.queue, msg => {
    if (msg.content) {
      console.log(`Message: ${msg.content.toString()}`);
    }

    }, { noAck: true })
}

const connect = (port) => {
  return `amqp://admin:admin@0.0.0.0:${port}`;
}

const localhost = () => {
  return `amqp://localhost`;
}

receiveMsg();

// node handlers/receiver.js 0 folha Estadao
// node handlers/receiver.js 5672 folha 
