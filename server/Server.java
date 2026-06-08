package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORTA = 5555;
    private static final GerenciadorPS gerenciador = new GerenciadorPS();

    public static void main(String[] args) {
        System.out.println("Iniciando o Servidor MicroSUS na porta " + PORTA);

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                // Aguarda a conexão de um cliente
                Socket clientSocket = serverSocket.accept();

                // Trata a requisição de forma iterativa
                processarRequisicao(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Erro crítico no servidor: " + e.getMessage());
        }
    }

    private static void processarRequisicao(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            HTTPParser parser = new HTTPParser();
            parser.parse(in);

            String metodo = parser.getMethod();
            String path = parser.getPath();
            String body = parser.getBody();

            System.out.println("Requisição recebida");
            enviarRespostaTexto(out, "200 OK", "application/json", "{\"status\":\"MicroSUS Ativo\"}");

        } catch (Exception e) {
            System.err.println("Erro ao processar fluxo do cliente: " + e.getMessage());
        } finally {
            try {
                socket.close(); // Garante o fechamento da conexão TCP
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket: " + e.getMessage());
            }
        }
    }

    // Método auxiliar para envio de respostas protocolo HTTP/1.1
    public static void enviarRespostaTexto(PrintWriter out, String status, String contentType, String conteudo) {
        out.print("HTTP/1.1 " + status + "\r\n");
        out.print("Content-Type: " + contentType + "; charset=UTF-8\r\n");
        out.print("Content-Length: " + conteudo.getBytes().length + "\r\n");
        out.print("Connection: close\r\n");
        out.print("\r\n");
        out.print(conteudo);
        out.flush();
    }
}
