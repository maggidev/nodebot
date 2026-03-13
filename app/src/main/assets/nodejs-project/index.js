
const { default: makeWASocket, useMultiFileAuthState, DisconnectReason, fetchLatestBaileysVersion, makeInMemoryStore, jidNormalizedUser, proto } = require('@whiskeysockets/baileys');
const { Boom } = require('@hapi/boom');
const http = require('http');
const { Server } = require('socket.io');

const PORT = 3000;
const store = makeInMemoryStore({ logger: console });

const server = http.createServer();
const io = new Server(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

io.on('connection', (socket) => {
    console.log('Socket connected:', socket.id);

    socket.on('start_pairing_process', async (phoneNumber) => {
        console.log('Starting pairing process for number:', phoneNumber);
        // Implement pairing code logic here
        // For now, just emit a dummy pairing code
        socket.emit('pairing_code_emit', '123-456-789');
    });

    socket.on('shutdown_bot', async () => {
        console.log('Shutting down bot...');
        if (sock) {
            await sock.logout();
        }
        process.exit(0);
    });
});

server.listen(PORT, () => {
    console.log(`Socket.io server listening on port ${PORT}`);
});

async function connectToWhatsApp() {
    const { state, saveCreds } = await useMultiFileAuthState('baileys_auth_info');
    const { version, is}; = await fetchLatestBaileysVersion();
    console.log(`using Baileys v${version.join('.')}`);

    const sock = makeWASocket({
        version,
        logger: console,
        printQRInTerminal: false,
        auth: state,
        browser: ['WhatsappBotContainer', 'Chrome', '10.0'],
    });

    store.bind(sock.ev);

    sock.ev.on('creds.update', saveCreds);

    sock.ev.on('connection.update', async (update) => {
        const { connection, lastDisconnect, qr } = update;

        if (qr) {
            io.emit('qr_code_emit', qr);
            console.log('QR Code received, emit to frontend');
        }

        if (connection === 'close') {
            let reason = new Boom(lastDisconnect?.error)?.output?.statusCode;
            if (reason === DisconnectReason.badAuthToken) {
                console.log('Bad Auth Token, Please Delete baileys_auth_info and Scan Again');
                io.emit('connection_status', 'Bad Auth Token');
                connectToWhatsApp();
            } else if (reason === DisconnectReason.connectionClosed) {
                console.log('Connection closed, reconnecting....');
                io.emit('connection_status', 'Connection Closed');
                connectToWhatsApp();
            } else if (reason === DisconnectReason.connectionLost) {
                console.log('Connection Lost from Server, reconnecting...');
                io.emit('connection_status', 'Connection Lost');
                connectToWhatsApp();
            } else if (reason === DisconnectReason.connectionReplaced) {
                console.log('Connection Replaced, Another New Session Opened, Please Close Current Session First');
                io.emit('connection_status', 'Connection Replaced');
                connectToWhatsApp();
            } else if (reason === DisconnectReason.loggedOut) {
                console.log('Device Logged Out, Please Delete baileys_auth_info and Scan Again.');
                io.emit('connection_status', 'Logged Out');
                connectToWhatsApp();
            } else if (reason === DisconnectReason.restartRequired) {
                console.log('Restart Required, reconnecting...');
                io.emit('connection_status', 'Restart Required');
                connectToWhatsApp();
            } else if (reason === DisconnectReason.timedOut) {
                console.log('Connection TimedOut, reconnecting...');
                io.emit('connection_status', 'Timed Out');
                connectToWhatsApp();
            } else {
                console.log(`Unknown DisconnectReason: ${reason}|${lastDisconnect.error}`);
                io.emit('connection_status', `Unknown DisconnectReason: ${reason}`);
                connectToWhatsApp();
            }
        } else if (connection === 'open') {
            console.log('opened connection');
            io.emit('connection_status', 'Connected');
        }
    });

    sock.ev.on('messages.upsert', async (m) => {
        console.log(JSON.stringify(m, undefined, 2));
        // You can add your bot logic here to respond to messages
    });

    return sock;
}

connectToWhatsApp();

// Capture console logs and emit them via Socket.io
const originalConsoleLog = console.log;
console.log = (...args) => {
    originalConsoleLog(...args);
    io.emit('console_log', args.join(' '));
};
