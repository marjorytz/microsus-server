package http;

import service.GerenciadorPS;
import model.Paciente;
import utils.JsonUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Router {

    private final GerenciadorPS gerenciador = new GerenciadorPS();

    public void processarRequisicao(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)) {
            HttpParser parser = new HttpParser();
            parser.parse(in);

            String metodo = parser.getMethod();
            String path = parser.getPath();
            String body = parser.getBody();

            if (metodo == null || path == null)
                return;

            // Rota 1: Cadastrar Paciente
            if (metodo.equals("POST") && path.equals("/pacientes")) {

                String nome = JsonUtil.extrairCampo(body, "nome");
                String sintoma = JsonUtil.extrairCampo(body, "sintoma");
                String prioridade = JsonUtil.extrairCampo(body, "prioridade");

                try {
                    Paciente novoPaciente = gerenciador.cadastrarPaciente(nome, sintoma, prioridade);
                    HttpResponse.enviar(out, "201 Created", "application/json", novoPaciente.toJson());
                } catch (IllegalArgumentException e) {
                    HttpResponse.enviar(out, "400 Bad Request", "application/json",
                            "{\"erro\":\"" + e.getMessage() + "\"}");
                }

                System.out.println("Requisição POST recebida");

                // Rota 2: Visualizar Fila
            } else if (metodo.equals("GET") && path.equals("/fila")) {
                List<Paciente> listaFila = gerenciador.getFilaOrdenada();
                String paginaHtml = HttpResponse.construirHtmlFila(listaFila);
                HttpResponse.enviar(out, "200 OK", "text/html", paginaHtml);

                System.out.println("Requisição GET recebida");

                // Rota Padrão (404)
            } else {
                HttpResponse.enviar(out, "404 Not Found", "application/json", "{\"erro\":\"Rota nao encontrada\"}");
            }

        } catch (Exception e) {
            System.err.println("Erro no Router: " + e.getMessage());
        } finally {
            try {
                socket.close(); // Garante o fechamento da conexão TCP
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket: " + e.getMessage());
            }
        }
    }
}
