import Fastify from "fastify";
import dotenv from "dotenv";

dotenv.config();

const app = Fastify({
    logger: true
});

app.get("/health", async () => {
    return {
        status: "ok"
    };
});

app.post("/api/chat", async (request, reply) => {

    const body = request.body as {
        message?: string;
    };

    const message = body.message?.trim();

    if (!message) {
        return reply.code(400).send({
            error: "Message is required"
        });
    }

    const normalizedMessage = message.toLowerCase();

    let botReply: string;

    switch (normalizedMessage) {

        case "halo":
        case "hai":
        case "hello":
            botReply =
                "Halo! Ada yang bisa saya bantu?";
            break;

        case "menu":
            botReply = `
Menu Bot:

1. halo
2. menu
3. info
4. tes
`.trim();
            break;

        case "info":
            botReply =
                "Saya adalah bot Android sederhana menggunakan Kotlin dan Jetpack Compose.";
            break;

        case "tes":
            botReply =
                "Bot sudah aktif!";
            break;

        default:
            botReply =
                "Maaf, saya belum mengerti pesan itu.";
            break;
    }

    return {
        reply: botReply
    };
});

const PORT = Number(process.env.PORT) || 3000;

app.listen({
    port: PORT,
    host: "0.0.0.0"
}).then(() => {
    console.log(`Backend running on port ${PORT}`);
}).catch((error) => {
    app.log.error(error);
    process.exit(1);
});