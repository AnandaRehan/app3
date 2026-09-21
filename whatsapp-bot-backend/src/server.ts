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