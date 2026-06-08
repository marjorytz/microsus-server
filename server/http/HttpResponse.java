package http;

import model.Paciente;
import java.io.PrintWriter;
import java.util.List;

public class HttpResponse {
    public static void enviar(PrintWriter out, String status, String contentType, String conteudo) {
        out.print("HTTP/1.1 " + status + "\r\n");
        out.print("Content-Type: " + contentType + "; charset=UTF-8\r\n");
        out.print("Content-Length: " + conteudo.getBytes().length + "\r\n");
        out.print("Connection: close\r\n");
        out.print("\r\n");
        out.print(conteudo);
        out.flush();
    }

    public static String construirHtmlFila(List<Paciente> fila) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='pt-BR'><head><meta charset='UTF-8'>");
        sb.append("<title>MicroSUS - Fila de Triagem</title>");
        sb.append(
                "<style>body{font-family:sans-serif; margin:30px;} table{width:100%; border-collapse:collapse;} th,td{border:1px solid #ccc; padding:10px;} th{background:#007941; color:white;}</style>");
        sb.append("</head><body><h1>Triagem - MicroSUS</h1>");

        if (fila.isEmpty()) {
            sb.append("<p>Nenhum paciente na fila.</p>");
        } else {
            sb.append("<table><tr><th>ID</th><th>Nome</th><th>Sintoma</th><th>Prioridade</th><th>Chegada</th></tr>");
            for (Paciente p : fila) {
                sb.append("<tr>")
                        .append("<td>").append(p.getId()).append("</td>")
                        .append("<td>").append(p.getNome()).append("</td>")
                        .append("<td>").append(p.getSintoma()).append("</td>")
                        .append("<td><b>").append(p.getPrioridade().toUpperCase()).append("</b></td>")
                        .append("<td>").append(p.getHoraChegada()).append("</td>")
                        .append("</tr>");
            }
            sb.append("</table>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }
}