const fs = require('fs');
const Redis = require('ioredis');
const Twitter = require('twitter');


let dictionary;
fs.readFile('words_dictionary.json', 'utf8', function (err, data) {
    if (err) throw err;
    dictionary = JSON.parse(data);
});
let commonWords;
fs.readFile('common_words2.txt', 'utf8', function (err, data) {
    if (err) throw err;
    commonWords = data.split(',');
});

const redisClient = new Redis.Cluster([
    {
        port: 6379,
        host: "10.0.3.8",
    },
    {
        port: 6381,
        host: "10.0.5.3", //2
    },
    {
        port: 6381,
        host: "10.0.4.2", //5
    },
], {
    maxRedirections: 10000,
    retryDelayOnClusterDown: 100000,
});

let twitterClient = new Twitter({
    consumer_key: process.env.TWITTER_CONSUMER_KEY,
    consumer_secret: process.env.TWITTER_CONSUMER_SECRET,
    access_token_key: process.env.TWITTER_ACCESS_TOKEN_KEY,
    access_token_secret: process.env.TWITTER_ACCESS_TOKEN_SECRET,
});

function fetchStream() {
    console.log('fetching...')
    twitterClient.stream('statuses/sample', function (stream) {
        stream.on('data', function (data) {
            const { text } = data;
            let words = text.split(" ");
            for (let w of words) {
                if ((w.length > 4 && w.length < 20 && !commonWords.includes(w)) && (dictionary.hasOwnProperty(w) || w.startsWith("#") || startsWithCapital(w))) {
                    redisClient.zincrby('words', 1, w);
                }
            }
        });

        stream.on('error', async function (error) {
            console.log(error)
            await sleep(1000);
        });
    });
}

let trending = { name: '', value: 0 };
setInterval(() => {
    redisClient.zrevrange('words', 1, 100, function (err, reply) {
        if (reply.length <= 20) trending = { name: '', value: 0 };
        for (let w of reply) {
            if (! /^[\x00-\x7F]*$/.test(w.substring(1)) || w.charAt('0') != '#')
                continue;
            redisClient.zscore('words', w, function (err, number) {
                redisClient.zadd('hashtags', parseInt(number), w);
                if (parseInt(number) > trending.value) {
                    trending.value = parseInt(number);
                    trending.name = w;
                    console.log(trending.name + ' ' + trending.value)
                }
            });
        }
    });
}, 5000);

fetchStream();


function startsWithCapital(word) {
    return word.charCodeAt(0) >= 65 && word.charCodeAt(0) <= 90
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}