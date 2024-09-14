package sisop_trab_1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.restassured.path.json.JsonPath;

public class Escalonador {

    private List<Processo> listaProcessos = new ArrayList<Processo>();
    private int tempoGlobal;
    private int tempoUsoCPU;
    private int processosFinalizados;

    /**
     * Prepara a execução do escalonador e executa o escalonamento dos processos
     */
    public void execucaoEscalonar() {
        tempoGlobal = 0; // tempo global do sistema
        tempoUsoCPU = 0; // tempo que a cpu foi utilizada
        processosFinalizados = 0; // quantidade de processos finalizados
        addProcesso(); // adiciona os processos do arquivo json
        escalonarProcesso(); // escalona os processos
    }

    /**
     * Adiciona os processos do arquivo json na lista de processos
     */
    public void addProcesso() {

        JsonPath jsonPath = new JsonPath(new File(System.getProperty("user.dir")
                + File.separator + "app"
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "resources"
                + File.separator + "processos.json"));

        for (int i = 0; i < jsonPath.getList("processos").size(); i++) {
            Processo processo1 = new Processo();
            processo1.setNomeProcesso(jsonPath.getString("processos[" + i + "].nome"));// nome do processo
            processo1.setSurtoCPU(jsonPath.getInt("processos[" + i + "].surtoCPU"));
            processo1.setTempoES(jsonPath.getInt("processos[" + i + "].tempoES"));
            processo1.setTempoTotal(jsonPath.getInt("processos[" + i + "].tempoTotal"));
            processo1.setOrdem(jsonPath.getInt("processos[" + i + "].ordem"));
            processo1.setPrioridade(jsonPath.getInt("processos[" + i + "].prioridade"));
            listaProcessos.add(processo1);
        }
    }

    public void escalonarProcesso() {

        while (!todosProcessosFinalizados()) {// enquanto houver processos na lista

            // olha se as condicoes para trocar processo running sao verdadeiras( se nao tem
            // processo running e se tem processo ready com credito maior que 0)
            if (!existeProcessoRunning() && existeProcessoReadyCreditoMaior()) {
                escolheProcessoRunning(); // troca de contexto
            }

            // vrifica se oc proecesso running vai para blocked ou se terminou
            if (existeProcessoRunning()) {
                verificarRunning();
            }

            // executa o processo running
            if (existeProcessoRunning()) {
                executaProcesso();

            } else { // caso nao exista nenhum processo running
                if (!existeProcessoRunning() && existeProcessoReadyCreditoMaior()) {
                    escolheProcessoRunning(); // acha um processo para rodar
                    executaProcesso(); // roda o processo
                } else {
                    System.out.println("-> Nenhum processo na fila de Ready");
                }
            }

            if (existeProcessoBlocked()) {
                voltaBloqueio();// verifica se tem processos bloqueados para voltar para a fila de ready
            }

            turnaroundTime();
            responseTime();

            tempoGlobal++;// todo ciclo ele aumenta o tempo de cpu

            printEscalonador();
        }

    }

    // monta a saida do escalonador
    public void printEscalonador() {

        String textoExecucao = "";

        double utilizacaoCpu = ((double) tempoUsoCPU / tempoGlobal) * 100;
        textoExecucao += String.format("Utilizacao de CPU TOTAL: %.2f%%\n", utilizacaoCpu);
        textoExecucao += String.format("Tempo que a CPU nao foi utilizada:  %.2f%%\n", (100 - utilizacaoCpu));

        double throughput = (double) processosFinalizados / tempoGlobal;
        textoExecucao += String.format("Throughput: %.2f processos/Unidade Tempo\n", throughput);

        for (int i = 0; i < listaProcessos.size(); i++) {
            Processo processo = listaProcessos.get(i);
            textoExecucao += "Processo " + processo.getNomeProcesso() +
                    "    ->    Estado: " + processo.getEstado() +
                    String.format("    ->    Tempo CPU:  %.2f", (((double) processo.getTempoCPU() / tempoGlobal) * 100))
                    +
                    "    ->    Turnaround Time: " + processo.getTurnaroundTime() +
                    "    ->    Credito: " + processo.getCredito() +
                    "    ->    Response Time: " + (processo.getResponseTime()) +
                    "    ->    Wait Time: " + processo.getWaitTime() + "\n";
        }

        printInBox(textoExecucao, "Escalonador = Tempo Global: " + tempoGlobal);

    }

    // deixar print mais bonitinho numa caixinha
    public static void printInBox(String message, String title) {
        String[] lines = message.split("\n");
        int maxLength = title.length();

        // Encontrar o comprimento da linha mais longa
        for (String line : lines) {
            if (line.length() > maxLength) {
                maxLength = line.length();
            }
        }

        String horizontalBorder = "+" + "-".repeat(maxLength + 2) + "+";
        String emptyLine = "|" + " ".repeat(maxLength + 2) + "|";

        // Centralizar o título
        int padding = (maxLength - title.length()) / 2;
        String centeredTitle = " ".repeat(padding) + title + " ".repeat(maxLength - title.length() - padding);

        System.out.println(horizontalBorder);
        System.out.println("| " + centeredTitle + " |");
        System.out.println(horizontalBorder);
        System.out.println(emptyLine);
        for (String line : lines) {
            System.out.println("| " + line + " ".repeat(maxLength - line.length()) + " |");
        }
        System.out.println(emptyLine);
        System.out.println(horizontalBorder);
        System.out.println("\n");
    }

    public void escolheProcessoRunning() {

        int creditoMax = 0; // credito de um processo da lista que será comparado
        int indice = 0; // indice do processo com maior credito maximo

        for (int i = 0; i < listaProcessos.size(); i++) {// achando o processo com maior prioridade
            Processo processo = listaProcessos.get(i);
            if (processo.getEstado() == Processo.Estado.READY) {
                int credito = processo.getCredito();
                if (credito > creditoMax) { // se o credito do processo for maior que o creditoMax
                    creditoMax = credito;
                    indice = i;
                } else {
                    if (credito == creditoMax) { // desempate pela ordem
                        if (processo.getOrdem() < listaProcessos.get(indice).getOrdem()) {
                            indice = i;
                        }
                    }
                }
            }
        }
        System.out
                .println("-> Processo " + listaProcessos.get(indice).getNomeProcesso() + " escolhido pelo Escalonador");
        listaProcessos.get(indice).setEstado(Processo.Estado.RUNNING);
        listaProcessos.get(indice).setResponse(true);
    }

    public void verificarRunning() {
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() == Processo.Estado.RUNNING) {

                if (listaProcessos.get(i).getTempoTotal() == 0) {
                    System.out.println("-> Processo " + listaProcessos.get(i).getNomeProcesso() + " finalizado");
                    listaProcessos.get(i).setEstado(Processo.Estado.EXIT);
                    todosProcessosCreditoZero(); // verifica se todos os processos da fila de ready estão com credito 0
                    listaProcessos.get(i).setExecutado(false);
                    processosFinalizados++;
                    return;
                }

                if (listaProcessos.get(i).getSurtoCPUAtual() == 0 && listaProcessos.get(i).getTempoES() > 0) {
                    listaProcessos.get(i).setEstado(Processo.Estado.BLOCKED);
                    System.out.println("-> Processo " + listaProcessos.get(i).getNomeProcesso()
                            + " movido para a fila de Bloqueados");
                    listaProcessos.get(i).setSurtoCPUAtual(listaProcessos.get(i).getSurtoCPU());
                    todosProcessosCreditoZero(); // verifica se todos os processos da fila de ready estão com credito 0

                    return;
                }

                if (listaProcessos.get(i).getCredito() == 0) {
                    System.out.println("-> Processo " + listaProcessos.get(i).getNomeProcesso()
                            + " movido para a fila de Ready");
                    listaProcessos.get(i).setEstado(Processo.Estado.READY);
                    mudarOrdemTodosProcessos(listaProcessos.get(i));
                    todosProcessosCreditoZero(); // verifica se todos os processos da fila de ready estão com credito 0

                    return;
                }

            }
        }
    }

    public void executaProcesso() {

        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() == Processo.Estado.RUNNING) {// achei o processo running
                listaProcessos.get(i).setSurtoCPUAtual(listaProcessos.get(i).getSurtoCPUAtual() - 1);
                listaProcessos.get(i).setTempoTotal(listaProcessos.get(i).getTempoTotal() - 1);
                listaProcessos.get(i).setCredito(listaProcessos.get(i).getCredito() - 1);
                tempoUsoCPU++;
                listaProcessos.get(i).setTempoCPU(listaProcessos.get(i).getTempoCPU() + 1);
                return;
            }
        }

    }

    public void voltaBloqueio() {

        for (int i = 0; i < listaProcessos.size(); i++) {

            if (listaProcessos.get(i).getEstado() == Processo.Estado.BLOCKED) {

                if (listaProcessos.get(i).getTempoESatual() == 0) {
                    System.out.println("-> Processo " + listaProcessos.get(i).getNomeProcesso()
                            + "foi movido para a fila de Ready");
                    listaProcessos.get(i).setEstado(Processo.Estado.READY);
                    mudarOrdemTodosProcessos(listaProcessos.get(i));
                    listaProcessos.get(i).setTempoESatual(listaProcessos.get(i).getTempoES());

                } else {
                    listaProcessos.get(i).setTempoESatual(listaProcessos.get(i).getTempoESatual() - 1);
                    listaProcessos.get(i).setWaitTime(listaProcessos.get(i).getWaitTime() + 1);

                    System.out.println("-> Processo " + listaProcessos.get(i).getNomeProcesso() +
                            " Tempo de E/S restante: " + listaProcessos.get(i).getTempoESatual() 
                                    
                                    
                                    );
                }
            }
        }

    }

    public void todosProcessosCreditoZero() {

        if (!existeProcessoReadyCreditoMaior()) {
            atualizaCredito(); // se todos os processos da fila de ready estiverem com credito 0, atualiza o
                               // credito
        }

    }

    public void atualizaCredito() {
        System.out.println("-> Atualizando os creditos de todos os Processos");
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() != Processo.Estado.EXIT) {
                int credito = (listaProcessos.get(i).getCredito() / 2) + listaProcessos.get(i).getPrioridade();
                listaProcessos.get(i).setCredito(credito);
            }
        }
    }

    public void mudarOrdemTodosProcessos(Processo processo) {

        listaProcessos.get(listaProcessos.indexOf(processo)).setOrdem(listaProcessos.size() - 1); // fica em ultimo da

        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.indexOf(processo) != i) {
                listaProcessos.get(i).setOrdem(listaProcessos.get(i).getOrdem() - 1);
            }
        }
    }

    public boolean existeProcessoRunning() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() == Processo.Estado.RUNNING) {
                return true; // Encontrou pelo menos um processo em estado RUNNING
            }
        }
        return false; // Nenhum processo em estado RUNNING foi encontrado
    }

    public boolean existeProcessoReadyCreditoMaior() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() == Processo.Estado.READY && processo.getCredito() > 0) {
                return true; // Encontrou pelo menos um processo em estado READY
            }
        }
        return false; // Nenhum processo em estado READY foi encontrado
    }

    public boolean existeProcessoBlocked() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() == Processo.Estado.BLOCKED) {
                System.out.println("-> Processo " + processo.getNomeProcesso() + " bloqueado");
                return true; // Encontrou pelo menos um processo em estado BLOCKED
            }
        }
        System.out.println("-> Nenhum processo bloqueado");
        return false; // Nenhum processo em estado BLOCKED foi encontrado
    }

    public boolean todosProcessosFinalizados() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() != Processo.Estado.EXIT) {
                return false; // Encontrou pelo menos um processo que não foi finalizado
            }
        }
        return true; // Todos os processos foram finalizados
    }

    public void turnaroundTime() {
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).isExecutado()) {
                listaProcessos.get(i).setTurnaroundTime(listaProcessos.get(i).getTurnaroundTime() + 1);
            }
        }
    }

    public void responseTime() {
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).isExecutado() && listaProcessos.get(i).isResponse()
                    && listaProcessos.get(i).getResponseTime() < 0) {
                listaProcessos.get(i).setResponse(true);
                listaProcessos.get(i).setResponseTime(tempoGlobal);
            }
        }
    }

}