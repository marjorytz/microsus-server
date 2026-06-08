package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HTTPParser {
    private Map<String, String> headers = new HashMap<>();
    private String method;
    private String path;
    private String httpVersion;
    private String body;

    public HTTPParser() {
        headers = new HashMap<>();
    }

    public void parse(BufferedReader in) throws IOException {
        // Extrair a linha de requisição
        String requestLine = in.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            return;
        }

        String[] partes = requestLine.split(" ");
        if (partes.length >= 3) {
            this.method = partes[0];
            this.path = partes[1];
            this.httpVersion = partes[2];
        }

        // Extrair os headers
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(":", 2);
            if (headerParts.length == 2) {
                this.headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        // Extrair o body
        if (this.headers.containsKey("Content-Length")) {
            int tamBody = Integer.parseInt(this.headers.get("Content-Length"));
            char[] buffer = new char[tamBody];

            int bytesLidos = in.read(buffer, 0, tamBody);

            if(bytesLidos > 0) 
                this.body = new String(buffer);
        }
    }

    // getters and setters
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(path).append(" ").append(httpVersion).append("\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("\n").append(body);
        return sb.toString();
    }
}