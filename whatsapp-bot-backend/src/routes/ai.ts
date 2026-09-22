import { FastifyInstance } from "fastify";

export async function aiRoutes(app: FastifyInstance) {
  app.post("/api/ai", async (request, reply) => {
    const body = request.body as {
      prompt?: string;
    };

    const prompt = body.prompt?.trim();

    if (!prompt) {
      return reply.code(400).send({
        error: "Prompt tidak boleh kosong."
      });
    }

    const apiKey = process.env.OPENAI_API_KEY;

    if (!apiKey) {
      return reply.code(500).send({
        error: "OPENAI_API_KEY belum dikonfigurasi."
      });
    }

    try {
      const response = await fetch(
        "https://api.openai.com/v1/responses",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${apiKey}`
          },
          body: JSON.stringify({
            model: "gpt-5.6-luna",
            input: prompt
          })
        }
      );

      const data = await response.json();

      if (!response.ok) {
        return reply.code(response.status).send({
          error: "OpenAI API error.",
          details: data
        });
      }

      return {
        text: data.output_text ?? ""
      };

    } catch (error) {
      return reply.code(500).send({
        error: "Gagal menghubungi OpenAI."
      });
    }
  });
}