package http;

import service.GerenciadorAuth;
import service.GerenciadorPS;
import model.Paciente;
import utils.JsonUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.NoSuchElementException;

public class Router {

    private final GerenciadorPS gerenciador = new GerenciadorPS();
    private final GerenciadorAuth auth = new GerenciadorAuth();

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

            // Barra qualquer POST que não seja a rota login
            if (metodo.equals("POST") && !path.equals("/login")) {
                String token = parser.getHeader("Authorization");

                if (!auth.validarToken(token)) {
                    HttpResponse.enviar(out, "401 Unauthorized", "application/json",
                            "{\"erro\":\"Acesso negado. Chave invalida ou ausente.\"}");
                    return;
                }
            }

            // Rota Bônus: Login
            if (metodo.equals("POST") && path.equals("/login")) {
                String usuario = JsonUtil.extrairCampo(body, "usuario");
                String senha = JsonUtil.extrairCampo(body, "senha");

                String token = auth.autenticar(usuario, senha);

                if (token != null) {
                    HttpResponse.enviar(out, "200 OK", "application/json", "{\"chave\":\"" + token + "\"}");
                    System.out.println("Requisição POST /login recebida");
                } else {
                    HttpResponse.enviar(out, "403 Forbidden", "application/json",
                            "{\"erro\":\"Credenciais invalidas\"}");
                }
            }

            // Rota 1: Cadastrar Paciente
            else if (metodo.equals("POST") && path.equals("/pacientes")) {

                String nome = JsonUtil.extrairCampo(body, "nome");
                String sintoma = JsonUtil.extrairCampo(body, "sintoma");
                String prioridade = JsonUtil.extrairCampo(body, "prioridade");

                try {
                    Paciente novoPaciente = gerenciador.cadastrarPaciente(nome, sintoma, prioridade);
                    HttpResponse.enviar(out, "201 Created", "application/json", novoPaciente.toJson());

                    System.out.println("Requisição POST /pacientes recebida");
                } catch (IllegalArgumentException e) {
                    HttpResponse.enviar(out, "400 Bad Request", "application/json",
                            "{\"erro\":\"" + e.getMessage() + "\"}");
                }
            }

            // Rota 2: Visualizar Fila
            else if (metodo.equals("GET") && path.equals("/fila")) {
                List<Paciente> listaFila = gerenciador.getFilaOrdenada();
                String paginaHtml = HttpResponse.construirHtmlFila(listaFila);
                HttpResponse.enviar(out, "200 OK", "text/html", paginaHtml);
                System.out.println("Requisição GET recebida");
            }

            // Rota 3: Visualizar paciente por ID
            else if (metodo.equals("GET") && path.startsWith("/pacientes/")) {

                try {
                    String[] idParam = path.split("/");
                    if (idParam.length < 3) {
                        throw new NumberFormatException("ID Ausente");
                    }

                    int idInt = Integer.parseInt(idParam[2]);
                    Paciente paciente = gerenciador.buscarPaciente(idInt);

                    if (paciente != null) {
                        HttpResponse.enviar(out, "200 OK", "application/json", paciente.toJson());
                        System.out.println("Requisição GET /pacientes/" + idInt + " recebida");
                    } else {
                        HttpResponse.enviar(out, "404 Not Found", "application/json",
                                "{\"erro\":\"Paciente não encontrado.\"}");
                    }

                } catch (IllegalArgumentException e) {
                    HttpResponse.enviar(out, "400 Bad Request", "application/json",
                            "{\"erro\":\"ID inválido ou ausente.\"}");
                }
            }

            // Rota 4: Chamar Próximo Paciente
            else if (metodo.equals("POST") && path.equals("/chamar")) {

                try {
                    Paciente proxPaciente = gerenciador.chamarProximo();

                    if (proxPaciente != null) {
                        HttpResponse.enviar(out, "200 OK", "application/json", proxPaciente.toJson());
                        System.out.println("Requisição POST /chamar recebida");
                    } else {
                        HttpResponse.enviar(out, "404 Not Found", "application/json",
                                "{\"erro\":\"Nenhum paciente na fila.\"}");
                    }
                } catch (IllegalArgumentException e) {
                    HttpResponse.enviar(out, "400 Bad Request", "application/json",
                            "{\"erro\":\"" + e.getMessage() + "\"}");
                }
            }

            // Rota 5: Finalizar Atendimento
            else if (metodo.equals("POST") && path.startsWith("/pacientes/") && path.endsWith("/finalizar")) {

                String prognostico = JsonUtil.extrairCampo(body, "prognostico");

                try {
                    String[] idParam = path.split("/");
                    if (idParam.length < 4) {
                        throw new NumberFormatException("ID Ausente");
                    }

                    int idInt = Integer.parseInt(idParam[2]);
                    Paciente pacienteFinalizado = gerenciador.finalizarAtendimento(idInt, prognostico);

                    HttpResponse.enviar(out, "200 OK", "application/json", pacienteFinalizado.toJson());
                    System.out.println("Requisição POST /finalizar recebida");

                } catch (NumberFormatException e) {
                    HttpResponse.enviar(out, "400 Bad Request", "application/json",
                            "{\"erro\":\"ID inválido ou ausente.\"}");
                } catch (NoSuchElementException e) {
                    // 404 se o paciente não existe
                    HttpResponse.enviar(out, "404 Not Found", "application/json",
                            "{\"erro\":\"" + e.getMessage() + "\"}");
                } catch (IllegalStateException e) {
                    HttpResponse.enviar(out, "409 Conflict", "application/json",
                            "{\"erro\":\"" + e.getMessage() + "\"}");
                }
            }

            // Rota 6: Mostrar Estatísticas
            else if (metodo.equals("GET") && path.equals("/estatisticas")) {

                String estatisticas = gerenciador.getEstatisticasJson();
                HttpResponse.enviar(out, "200 OK", "application/json", estatisticas);
                System.out.println("Requisição GET recebida");
            }
            
            // Rota Padrão (404)
            else {
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
