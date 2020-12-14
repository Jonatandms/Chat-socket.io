// Setup basic express server
var express = require('express');
var app = express();
var path = require('path');
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var port = process.env.PORT || 3000;

server.listen(port, () => {
    console.log('Server listening at port %d', port);
});

// Routing
app.use(express.static(path.join(__dirname, 'public')));

// Chatroom

var numUsers = 0;
var users = [];

io.on('connection', (socket) => {
    var addedUser = false;
    console.log("usuario conectado");

    // when the client emits 'new message', this listens and executes
    socket.on('new message', (data) => {
        console.log("nuevo mensaje");
        // we tell the client to execute 'new message'
        socket.broadcast.emit('new message', {
            username: socket.username,
            message: data
        });
    });

    socket.on('join', (room) => {

        socket.join(room);
		socket.room = room;
        console.log("EL SOCKET SE HA UNIDO A LA SALA", room);

    })
    socket.on('join me', (id) => {
		console.log("EMITIENDO MENSAJE")
		socket.to(id).emit('join me', id + ';' + socket.username);
     /*   console.log("socket.id" + socket.id);
        console.log("id" + id);
        if (socket.id == id) {
            console.log("UNIENDOME A LA SALA PRIVADA");
            socket.join("room" + id);
        }*/
    })
    socket.on('private message', (data) => {
        console.log(data);
        socket.to(socket.room).emit('update', data);

    })


    // when the client emits 'add user', this listens and executes
    socket.on('add user', (username) => {
        if (addedUser) return;
        console.log(username);
        // we store the username in the socket session for this client
        socket.username = username;
        ++numUsers;
        users.push({
            id: socket.id,
            username: socket.username
        });
        addedUser = true;
        socket.emit('login', {
            numUsers: numUsers
        });
        // echo globally (all clients) that a person has connected
        socket.broadcast.emit('user joined', {
            username: socket.username,
            numUsers: numUsers
        });
    });

    // when the client emits 'typing', we broadcast it to others
    socket.on('typing', () => {
        socket.broadcast.emit('typing', {
            username: socket.username
        });
    });

    // when the client emits 'stop typing', we broadcast it to others
    socket.on('stop typing', () => {
        socket.broadcast.emit('stop typing', {
            username: socket.username
        });
    });
    // GESTIONAR PARES DE USUARIO CON EL ID
    //Cuando el usuario pregunta cuantos usuarios hay conectados
    socket.on('users connected', () => {
        console.log(users)
        socket.emit('users', {
            users: users
        });

    });



    // when the user disconnects.. perform this
    socket.on('disconnect', () => {

        if (addedUser) {
            --numUsers;
            users = users.filter(u => (u.id != socket.id));
            console.log("usuario desconectado", numUsers);
            // echo globally that this client has left
            socket.broadcast.emit('user left', {
                username: socket.username,
                numUsers: numUsers
            });
        }
    });

});