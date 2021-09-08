const amqplib = require('amqplib');

const queueName = "wdj";
const msg = "comment";

const sendMsg = async () => {
  const connection = await amqplib.connect('amqp://admin:admin@0.0.0.0:5672');
  const channel = await connection.createChannel();
  await channel.assertQueue(queueName, {durable: false});
  channel.sendToQueue(queueName, Buffer.from(msg));
  console.log('Sent: ', msg);
  setTimeout(() => {
    connection.close();
    process.exit(0);
  }, 500)
}

sendMsg();
