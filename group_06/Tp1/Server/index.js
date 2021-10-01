const net = require("net")
let jsonData = require('./names.json');

const server = net.createServer(function (socket) {
    Array.prototype.random = function () {
        return this[Math.floor((Math.random()*20))];
    }
  
    console.log('entrei')

    let count = 0;
    while(count <= 1000){
      sort1 = jsonData.random() + " " 
        + jsonData.random() + " " + jsonData.random() + "\n"
    
      socket.write(sort1)
      count += 1
    }    

    socket.end()

})

server.on("error", (err) => {
    console.log(`Error in server:\n${err}`);
});
  
server.listen(3333, "0.0.0.0", () => {
    console.log("Nodejs server ready to respond.");
});