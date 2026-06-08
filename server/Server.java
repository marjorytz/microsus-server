
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import http.Router;

public class Server {

    private static final int PORTA = 5555;
    private static final Router router = new Router();

    public static void main(String[] args) {
        System.out.println("Iniciando o Servidor MicroSUS na porta " + PORTA);

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                // Aguarda a conexão de um cliente
                Socket clientSocket = serverSocket.accept();

                router.processarRequisicao(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Erro crítico no servidor: " + e.getMessage());
        }
    }
}
