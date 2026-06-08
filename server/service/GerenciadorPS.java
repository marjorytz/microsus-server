package service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import model.EstadoPaciente;
import model.Paciente;

public class GerenciadorPS {
    private final Map<Integer, Paciente> mapaPacientes = new ConcurrentHashMap<>();
    private final PriorityQueue<Paciente> filaAtendimento = new PriorityQueue<>();
    private final AtomicInteger geradorId = new AtomicInteger(1);

    // Métodos
    // 1. Cadastrar Paciente (POST /pacientes)
    public synchronized Paciente cadastrarPaciente(String nome, String sintoma, String prioridade) {
        if (nome == null || sintoma == null || prioridade == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Campos obrigatórios ausentes ou inválidos.");
        }

        int id = geradorId.getAndIncrement();
        Paciente paciente = new Paciente(id, nome, sintoma, prioridade);

        mapaPacientes.put(id, paciente);
        filaAtendimento.add(paciente);

        return paciente;
    }

    // 2. Chamar Próximo Paciente (POST /chamar)
    public synchronized Paciente chamarProximo() {
        Paciente proximo = filaAtendimento.poll();

        if (proximo == null) {
            return null;
        }

        proximo.setEstado(EstadoPaciente.EM_ATENDIMENTO);
        return proximo;
    }

    // 3. Finalizar Atendimento (POST /pacientes/{id}/finalizar)
    public synchronized Paciente finalizarAtendimento(int id, String prognostico) {
        Paciente paciente = mapaPacientes.get(id);

        if (paciente == null) {
            throw new NoSuchElementException("Paciente com ID " + id + " não encontrado.");
        }

        if (paciente.getEstado() != EstadoPaciente.EM_ATENDIMENTO) {
            throw new IllegalStateException(
                    "Paciente está EM_FILA. Não é possível finalizar sem antes chamar para atendimento.");
        }

        if (paciente.getEstado() == EstadoPaciente.ATENDIDO) {
            throw new IllegalStateException("Paciente já está ATENDIDO. Não há transição possível.");
        }

        paciente.setEstado(EstadoPaciente.ATENDIDO);
        paciente.setPrognostico(prognostico);
        return paciente;
    }

    // 4. Consultar Paciente (GET /pacientes/{id})
    public Paciente buscarPaciente(int id) {
        return mapaPacientes.get(id);
    }

    // 5. Retornar a Fila (GET /fila)
    public synchronized List<Paciente> getFilaOrdenada() {
        PriorityQueue<Paciente> copiaFila = new PriorityQueue<>(this.filaAtendimento);
        List<Paciente> listaOrdenada = new ArrayList<>();

        while (!copiaFila.isEmpty()) {
            listaOrdenada.add(copiaFila.poll());
        }
        return listaOrdenada;
    }

    // 6. Métricas (GET /estatisticas)
    public synchronized String getEstatisticasJson() {
        int emFila = 0, emAtendimento = 0, atendido = 0;
        int vermelho = 0, amarelo = 0, verde = 0;

        for (Paciente p : mapaPacientes.values()) {
            if (p.getEstado() == EstadoPaciente.EM_FILA)
                emFila++;
            else if (p.getEstado() == EstadoPaciente.EM_ATENDIMENTO)
                emAtendimento++;
            else if (p.getEstado() == EstadoPaciente.ATENDIDO)
                atendido++;

            if (p.getPrioridade().equals("vermelho"))
                vermelho++;
            else if (p.getPrioridade().equals("amarelo"))
                amarelo++;
            else if (p.getPrioridade().equals("verde"))
                verde++;
        }

        return "{" +
                "\"totalGeral\":" + mapaPacientes.size() + "," +
                "\"porEstado\":{" +
                "\"EM_FILA\":" + emFila + "," +
                "\"EM_ATENDIMENTO\":" + emAtendimento + "," +
                "\"ATENDIDO\":" + atendido +
                "}," +
                "\"porPrioridade\":{" +
                "\"vermelho\":" + vermelho + "," +
                "\"amarelo\":" + amarelo + "," +
                "\"verde\":" + verde +
                "}" +
                "}";
    }
}
