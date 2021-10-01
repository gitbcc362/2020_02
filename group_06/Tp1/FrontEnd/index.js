const express = require('express');

const app = express();
app.use(express.json());
const router = express.Router();
var chaves = []
const PORT = process.env.PORT || 4444;
const HOST = '0.0.0.0';

app.use(
    router.get('/', (req, res) => {
        return res.json({
            chaves: chaves.chave
        })
    }),

    router.post('/update', (req, res) => {
        chaves = req.body;
        res.send(true);
        return;
    })
)

app.set('PORT', PORT);

const server = app.listen(app.get('PORT'), () => {
    console.log(`Server running on port ${app.get('PORT')}`)
})

module.exports = {
    server
}