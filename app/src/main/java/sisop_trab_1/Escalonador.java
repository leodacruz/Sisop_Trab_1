package sisop_trab_1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.restassured.path.json.JsonPath;

public class Escalonador {

    private List<Processo> listaProcessos;
    private int tempoGlobal;
    private int tempoUsoCPU;
    private int processosFinalizados;
    private int tamanhoOriginalListaProcessos;
    private String tempoNaoExecutado;
    private String escalonadorRodando;
    private boolean processosSaoAleatorios;
    private boolean existeProcessoRunning;

    public Escalonador() {
        listaProcessos = new ArrayList<Processo>();
        tempoGlobal = 0;
        tempoUsoCPU = 0;
        processosFinalizados = 0;
        tempoNaoExecutado = "";
        escalonadorRodando = "";
    }

    // Adiciona os processos na lista
    public void addProcesso() {
        processosSaoAleatorios = false;
        JsonPath jsonPath = new JsonPath(new File(System.getProperty("user.dir")
                // + File.separator + "app"
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "resources"
                + File.separator + "processos.json"));

        for (int i = 0; i < jsonPath.getList("processos").size(); i++) {
            Processo processo1 = new Processo();
            processo1.setNomeProcesso(jsonPath.getString("processos[" + i + "].nome"));
            processo1.setSurtoCPU(jsonPath.getInt("processos[" + i + "].surtoCPU"));
            processo1.setTempoES(jsonPath.getInt("processos[" + i + "].tempoES"));
            processo1.setTempoTotal(jsonPath.getInt("processos[" + i + "].tempoTotal"));
            processo1.setOrdem(jsonPath.getInt("processos[" + i + "].ordem"));
            processo1.setPrioridade(jsonPath.getInt("processos[" + i + "].prioridade"));
            listaProcessos.add(processo1);
        }
    }

    public void addProcessoAleatorio(int quantidade) {
        processosSaoAleatorios = true;
        Random random = new Random(System.nanoTime());
        int ordemAtual = listaProcessos.size() + 1;

        for (int i = 0; i < quantidade; i++) {
            String nome = "P" + ordemAtual;

            int surtoCPU = 0;
            surtoCPU = random.nextInt(10); // Valor aleatório entre 0 e 9
            int tempoES = 0;
            if (surtoCPU > 0) {
                tempoES = random.nextInt(10) + 1; // Valor aleatório entre 1 e 10
            }

            int tempoTotal = random.nextInt(10) + 1; // Valor aleatório entre 1 e 20
            int prioridade = random.nextInt(10); // Valor aleatório entre 1 e 10

            Processo processo = new Processo();
            processo.setNomeProcesso(nome);
            processo.setSurtoCPU(surtoCPU);
            processo.setTempoES(tempoES);
            processo.setTempoTotal(tempoTotal);
            processo.setOrdem(ordemAtual);
            processo.setPrioridade(prioridade);

            listaProcessos.add(processo);
            ordemAtual++;
        }

    }

    // Print bonitinho
    public void printEscalonador() {

        if (!processosSaoAleatorios) { // se for execucao alaeatoria
            printNaCaixinha(escalonadorRodando, "Escalonador Trabalhando");
        }

        String textoExecucao = "";

        double utilizacaoCpu = ((double) tempoUsoCPU / tempoGlobal) * 100;
        textoExecucao += String.format("Utilizacao de CPU TOTAL: %.2f%%\n", utilizacaoCpu);

        textoExecucao += String.format("Tempo que a CPU nao foi utilizada:  %.2f%%\n", (100 - utilizacaoCpu));
        if (!tempoNaoExecutado.isEmpty()) {
            String auxliar = centralizaTexto(tempoNaoExecutado, 80);
            textoExecucao += "Tempos que a CPU nao foi utilizada: " + auxliar + "\n";
        }

        double mediaWaitingTime = 0;
        for (int i = 0; i < listaProcessos.size(); i++) {
            mediaWaitingTime += listaProcessos.get(i).getWaitingTime();
        }
        mediaWaitingTime = mediaWaitingTime / listaProcessos.size();
        textoExecucao += String.format("Tempo Medio de Waiting Time: %.2f unidades de Tempo\n", mediaWaitingTime);

        double throughput = (double) processosFinalizados / tempoGlobal;
        textoExecucao += String.format("Throughput: %.2f processos/Unidade Tempo\n\n", throughput);

        if (!processosSaoAleatorios) {

            textoExecucao += String.format("%-10s %-10s %-15s %-20s %-10s %-15s %-15s %-10s %-15s\n",
                    "Processo", "Estado", "Tempo CPU", "Turnaround Time", "Credito", "Response Time", "Waiting Time",
                    "Ordem", "Prioridade");

            for (int i = 0; i < listaProcessos.size(); i++) {
                Processo processo = listaProcessos.get(i);
                textoExecucao += String.format("%-10s %-10s %-15s %-20s %-10s %-15s %-15s %-10s %-15s\n",
                        processo.getNomeProcesso(),
                        processo.getEstado(),
                        String.format("%.2f%%", (((double) processo.getTempoCPU() / tempoGlobal) * 100)),
                        processo.getTurnaroundTime(),
                        processo.getCredito(),
                        processo.getResponseTime(),
                        processo.getWaitingTime(),
                        processo.getOrdem(),
                        processo.getPrioridade());
            }
        }

        printNaCaixinha(textoExecucao, "Escalonador = Tempo Global: " + tempoGlobal);

    }

    public void printNaCaixinha(String message, String title) {
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

    public String centralizaTexto(String text, int lineLength) {
        StringBuilder wrappedText = new StringBuilder();
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + lineLength, length);
            wrappedText.append(text, start, end).append("\n");
            start = end;
        }

        return wrappedText.toString();
    }

    // Execução
    public void execucaoEscalonar(String[] args) {
        if (args.length > 0) {
            try {
                int quantidade = Integer.parseInt(args[0]);
                addProcessoAleatorio(quantidade);
                System.out.println("Adicionando " + quantidade + " processos aleatorios.\n");
            } catch (NumberFormatException e) {
                System.out.println("Erro: Argumento nao e um número valido. Usando o json padrao.\n");
                addProcesso();
            }
        } else {
            addProcesso();
        }
        tamanhoOriginalListaProcessos = listaProcessos.size(); // otmiização para while de escalonarProcesso
        escalonarProcesso();
    }

    public void escalonarProcesso() {

        while (processosFinalizados < tamanhoOriginalListaProcessos) {
            escalonadorRodando = ""; // limpa o print do escalonador rodando a cada ciclo

            verificarRunning();
            if (!existeProcessoRunning && existeProcessoReadyCreditoMaior()) {
                escolheProcessoRunning();
            }
            executaProcesso();
            voltaBloqueio();

            metricas();
            tempoGlobal++;

            if (!processosSaoAleatorios) {// se os processos não forem aleatorios printa a cada ciclo
                printEscalonador();
            }
        }

        if (processosSaoAleatorios) { // se os processos forem aleatorios printa apos a execucao
            printEscalonador();
        }
    }

    public void verificarRunning() {
        escalonadorRodando += "o-> Verificando Processo Running\n";
        existeProcessoRunning = false;
        for (int i = 0; i < listaProcessos.size(); i++) {

            if (listaProcessos.get(i).getEstado() == Processo.Estado.RUNNING) {
                existeProcessoRunning = true;

                if (listaProcessos.get(i).getTempoTotal() == 0) {
                    escalonadorRodando += "---> Processo " + listaProcessos.get(i).getNomeProcesso() + " Finalizado\n";
                    listaProcessos.get(i).setEstado(Processo.Estado.EXIT);
                    todosProcessosCreditoZero();
                    listaProcessos.get(i).setExecutado(false);
                    processosFinalizados++;
                    existeProcessoRunning = false;
                    return;
                }

                if (listaProcessos.get(i).getSurtoCPUAtual() == 0 && listaProcessos.get(i).getTempoES() > 0) {
                    listaProcessos.get(i).setEstado(Processo.Estado.BLOCKED);
                    escalonadorRodando += "---> Processo " + listaProcessos.get(i).getNomeProcesso()
                            + " movido para a fila de Bloqueados\n";
                    listaProcessos.get(i).resetSurtoCPUatual();
                    todosProcessosCreditoZero();
                    existeProcessoRunning = false;
                    return;
                }

                if (listaProcessos.get(i).getCredito() == 0) {
                    escalonadorRodando += "---> Processo " + listaProcessos.get(i).getNomeProcesso()
                            + " movido para a fila de Ready\n";
                    listaProcessos.get(i).setEstado(Processo.Estado.READY);
                    mudarOrdemTodosProcessos(listaProcessos.get(i));
                    todosProcessosCreditoZero();
                    existeProcessoRunning = false;
                    return;
                }
            }
        }
    }

    public void escolheProcessoRunning() {
        escalonadorRodando += "o-> Escolhendo Processo\n";

        int prioridadeMax = 0;
        int creditoMax = 0;
        int indice = 0;

        for (int i = 0; i < listaProcessos.size(); i++) {
            Processo processo = listaProcessos.get(i);
            if (processo.getEstado() == Processo.Estado.READY) {
                int credito = processo.getCredito();
                int prioridade = processo.getPrioridade();

                if (credito > creditoMax) {
                    if (prioridade >= prioridadeMax) { // tratar que um processo prioridade 0 nao rode antes de um 1
                        creditoMax = credito;
                        indice = i;
                    }
                } else {
                    if (credito == creditoMax) { // desempate pela ordem
                        if (processo.getOrdem() < listaProcessos.get(indice).getOrdem()) {
                            indice = i;
                        }
                    }
                }
            }
        }

        escalonadorRodando += "o-> Processo " + listaProcessos.get(indice).getNomeProcesso()
                + " escolhido pelo Escalonador\n";
        listaProcessos.get(indice).setEstado(Processo.Estado.RUNNING);
        listaProcessos.get(indice).setResponse(true);
    }

    public void executaProcesso() {
        boolean cpuNaoUtilizada = true;
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() == Processo.Estado.RUNNING) {
                escalonadorRodando += "o-> Executando Processo " + listaProcessos.get(i).getNomeProcesso() + "\n";
                listaProcessos.get(i).decSurtoCPUAtual();
                listaProcessos.get(i).decTempoTotal();
                listaProcessos.get(i).decCredito();
                tempoUsoCPU++;
                listaProcessos.get(i).incTempoCPU();
                cpuNaoUtilizada = false;
                return;
            }
        }

        if (cpuNaoUtilizada) {
            tempoNaoExecutado += (tempoGlobal + 1) + " ; ";
        }
    }

    public void voltaBloqueio() {
        escalonadorRodando += "o-> Verificando Processos Bloqueados\n";
        for (int i = 0; i < listaProcessos.size(); i++) {

            if (listaProcessos.get(i).getEstado() == Processo.Estado.BLOCKED) {
                if (listaProcessos.get(i).getTempoESatual() == 0) {
                    escalonadorRodando += "---> Processo " + listaProcessos.get(i).getNomeProcesso()
                            + " movido para a fila de Ready\n";
                    listaProcessos.get(i).setEstado(Processo.Estado.READY);
                    mudarOrdemTodosProcessos(listaProcessos.get(i));
                    listaProcessos.get(i).resetTempoESatual();
                    todosProcessosCreditoZero(); // verifica se todos os processos da fila de ready estão com credito 0
                } else {
                    listaProcessos.get(i).decTempoESatual();

                    escalonadorRodando += "---> Processo " + listaProcessos.get(i).getNomeProcesso() +
                            " Tempo de E/S restante: " + listaProcessos.get(i).getTempoESatual() + "\n";
                }
            }
        }
    }

    // Metodos auxiliares
    public void todosProcessosCreditoZero() {
        escalonadorRodando += "o-> Verificando se todos os Processos da fila de Ready estao com credito 0\n";
        if (!existeProcessoReadyCreditoMaior()) {
            atualizaCredito();
        }
    }

    public void atualizaCredito() {
        escalonadorRodando += "---> Atualizando os creditos de todos os Processos\n";
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() != Processo.Estado.EXIT) {
                if (listaProcessos.get(i).getPrioridade() > 0) {
                    int credito = (listaProcessos.get(i).getCredito() / 2) + listaProcessos.get(i).getPrioridade();
                    listaProcessos.get(i).setCredito(credito);
                } else {
                    int credito = (listaProcessos.get(i).getCredito() / 2) + 1;
                    listaProcessos.get(i).setCredito(credito);
                }
            }
        }
    }

    public void mudarOrdemTodosProcessos(Processo processo) {
        escalonadorRodando += "o-> Mudando a ordem de todos os Processos\n";
        listaProcessos.get(listaProcessos.indexOf(processo)).setOrdem(tamanhoOriginalListaProcessos); // fica em ultimo
                                                                                                      // da

        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.indexOf(processo) != i) {
                listaProcessos.get(i).setOrdem(listaProcessos.get(i).getOrdem() - 1);
            }
        }
    }

    public boolean existeProcessoReadyCreditoMaior() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() == Processo.Estado.READY && processo.getCredito() > 0) {
                return true;
            }
        }
        return false;
    }

    // Metricas
    public void metricas() {
        for (int i = 0; i < listaProcessos.size(); i++) {
            
            // waiting time
            if (listaProcessos.get(i).getEstado() == Processo.Estado.READY) {
                listaProcessos.get(i).incWaitingTime();
            }

            // se nao tiver rodando os aleatorios
            if (!processosSaoAleatorios) {
                
                // turnaround time
                if (listaProcessos.get(i).isExecutado()) {
                    listaProcessos.get(i).incTurnaroundTime();
                }

                // response time
                if (listaProcessos.get(i).isResponse() && listaProcessos.get(i).getResponseTime() < 0) {
                    listaProcessos.get(i).setResponseTime(tempoGlobal);
                }
            }
        }
    }

}