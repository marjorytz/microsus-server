package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Paciente implements Comparable<Paciente> {
    private int id;
    private String nome;
    private String sintoma;
    private String prioridade;
    private EstadoPaciente estado;
    private String horaChegada;
    private String prognostico;

    public Paciente(int id, String nome, String sintoma, String prioridade) {
        this.id = id;
        this.nome = nome;
        this.sintoma = sintoma;
        this.prioridade = prioridade.toLowerCase();
        this.estado = EstadoPaciente.EM_FILA;
        this.horaChegada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        this.prognostico = null;
    }

    @Override
    public int compareTo(Paciente outro) {
        int pesoEste = getPesoPrioridade(this.prioridade);
        int pesoOutro = getPesoPrioridade(outro.prioridade);

        if (pesoEste != pesoOutro) {
            return Integer.compare(pesoEste, pesoOutro);
        }

        return Integer.compare(this.id, outro.id);
    }

    private int getPesoPrioridade(String prioridade) {
        switch (prioridade) {
            case "vermelho":
                return 1;
            case "amarelo":
                return 2;
            case "verde":
                return 3;
            default:
                return 4;
        }
    }

    public String toJson() {
        return "{" +
                "\"id\":" + id + "," +
                "\"nome\":\"" + nome + "\"," +
                "\"sintoma\":\"" + sintoma + "\"," +
                "\"prioridade\":\"" + prioridade + "\"," +
                "\"estado\":\"" + estado.name() + "\"," +
                "\"horaChegada\":\"" + horaChegada + "\"," +
                "\"prognostico\":" + (prognostico == null ? "null" : "\"" + prognostico + "\"") +
                "}";
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSintoma() {
        return sintoma;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public EstadoPaciente getEstado() {
        return estado;
    }

    public void setEstado(EstadoPaciente estado) {
        this.estado = estado;
    }

    public String getHoraChegada() {
        return horaChegada;
    }

    public String getPrognostico() {
        return prognostico;
    }

    public void setPrognostico(String prognostico) {
        this.prognostico = prognostico;
    }
}
