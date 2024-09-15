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

    
    public void execucaoEscalonar() {
        tempoGlobal = 0; // tempo global do sistema
        tempoUsoCPU = 0; // tempo que a cpu foi utilizada
        processosFinalizados = 0; // quantidade de processos finalizados
        addProcesso(); // adiciona os processos do arquivo json
        escalonarProcesso(); // escalona os processos
    }

    public void addProcesso()  {
        JsonPath jsonPath = new JsonPath(new File(System.getProperty("user.dir")
              //  + File.separator + "app" //Para funcionar o gradlew run
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

    public void escalonarProcesso() {

        while (!todosProcessosFinalizados()) {

            verificarRunning();

            if (!existeProcessoRunning() && existeProcessoReadyCreditoMaior()) {
                escolheProcessoRunning();
            }

            executaProcesso();

            voltaBloqueio();// verifica se tem processos bloqueados para voltar para a fila de ready
          
            turnaroundTime();
            responseTime();

            tempoGlobal++;// todo ciclo ele aumenta o tempo de cpu

            printEscalonador();
        }

    }

    public void printEscalonador() {

        String textoExecucao = "";

        double utilizacaoCpu = ((double) tempoUsoCPU / tempoGlobal) * 100;
        textoExecucao += String.format("Utilizacao de CPU TOTAL: %.2f%%\n", utilizacaoCpu);
        textoExecucao += String.format("Tempo que a CPU nao foi utilizada:  %.2f%%\n", (100 - utilizacaoCpu));

        double throughput = (double) processosFinalizados / tempoGlobal;
        textoExecucao += String.format("Throughput: %.2f processos/Unidade Tempo\n\n", throughput);

        textoExecucao += String.format("%-15s %-10s %-15s %-20s %-10s %-15s %-15s %-10s %-15s\n",
                "Processo", "Estado", "Tempo CPU", "Turnaround Time", "Credito", "Response Time", "Wait Time", "Ordem","Prioridade");

        for (int i = 0; i < listaProcessos.size(); i++) {
            Processo processo = listaProcessos.get(i);
            textoExecucao += String.format("%-15s %-10s %-15s %-20s %-10s %-15s %-15s %-10s %-15s\n",
                    processo.getNomeProcesso(),
                    processo.getEstado(),
                    String.format("%.2f%%", (((double) processo.getTempoCPU() / tempoGlobal) * 100)),
                    processo.getTurnaroundTime(),
                    processo.getCredito(),
                    processo.getResponseTime(),
                    processo.getWaitTime(),
                    processo.getOrdem(),
                    processo.getPrioridade());
        }

        printInBox(textoExecucao, "Escalonador = Tempo Global: " + tempoGlobal);

    }

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
        System.out.println("o-> Escolhendo Processo" );
        
        int prioridadeMax=0;
        int creditoMax = 0; 
        int indice = 0; 

        for (int i = 0; i < listaProcessos.size(); i++) {
            Processo processo = listaProcessos.get(i);
            if (processo.getEstado() == Processo.Estado.READY) {
                
                int credito = processo.getCredito();
                int prioridade=processo.getPrioridade();

                if (credito > creditoMax) { 

                    if(prioridade>=prioridadeMax){ //tratar que um processo prioridade 0 nao rode antes de um 1
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
        System.out.println("---> Processo " + listaProcessos.get(indice).getNomeProcesso() + " escolhido pelo Escalonador");
        listaProcessos.get(indice).setEstado(Processo.Estado.RUNNING);
        listaProcessos.get(indice).setResponse(true);
    }

    public void verificarRunning() {
        System.out.println("o-> Verificando Processo Running");
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() == Processo.Estado.RUNNING) {

                if (listaProcessos.get(i).getTempoTotal() == 0) {
                    System.out.println("---> Processo " + listaProcessos.get(i).getNomeProcesso() + " Finalizado");
                    listaProcessos.get(i).setEstado(Processo.Estado.EXIT);
                    todosProcessosCreditoZero(); // verifica se todos os processos da fila de ready estão com credito 0
                    listaProcessos.get(i).setExecutado(false);
                    processosFinalizados++;
                    return;
                }

                if (listaProcessos.get(i).getSurtoCPUAtual() == 0 && listaProcessos.get(i).getTempoES() > 0) {
                    listaProcessos.get(i).setEstado(Processo.Estado.BLOCKED);
                    System.out.println("---> Processo " + listaProcessos.get(i).getNomeProcesso()
                            + " movido para a fila de Bloqueados");
                    listaProcessos.get(i).setSurtoCPUAtual(listaProcessos.get(i).getSurtoCPU());
                    todosProcessosCreditoZero(); // verifica se todos os processos da fila de ready estão com credito 0

                    return;
                }

                if (listaProcessos.get(i).getCredito() == 0) {
                    System.out.println("---> Processo " + listaProcessos.get(i).getNomeProcesso()
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
            if (listaProcessos.get(i).getEstado() == Processo.Estado.RUNNING) {
                System.out.println("o-> Executando Processo " + listaProcessos.get(i).getNomeProcesso());
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
        System.out.println("o-> Verificando Processos Bloqueados");
        for (int i = 0; i < listaProcessos.size(); i++) {
            
            if (listaProcessos.get(i).getEstado() == Processo.Estado.BLOCKED) {

                if (listaProcessos.get(i).getTempoESatual() == 0) {
                    System.out.println("---> Processo " + listaProcessos.get(i).getNomeProcesso()
                            + " foi movido para a fila de Ready");
                    listaProcessos.get(i).setEstado(Processo.Estado.READY);
                    mudarOrdemTodosProcessos(listaProcessos.get(i));
                    listaProcessos.get(i).setTempoESatual(listaProcessos.get(i).getTempoES());

                } else {
                    listaProcessos.get(i).setTempoESatual(listaProcessos.get(i).getTempoESatual() - 1);
                    listaProcessos.get(i).setWaitTime(listaProcessos.get(i).getWaitTime() + 1);

                    System.out.println("---> Processo " + listaProcessos.get(i).getNomeProcesso() +
                            " Tempo de E/S restante: " + listaProcessos.get(i).getTempoESatual()

                    );
                }
            }
        }

    }

    public void todosProcessosCreditoZero() {
        System.out.println("o-> Verificando se todos os Processos da fila de Ready estao com credito 0");

        boolean creditoZero = true;
        boolean processoReady = false;

        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() == Processo.Estado.READY) {
                processoReady = true;
                if (listaProcessos.get(i).getCredito() != 0) {
                    creditoZero = false;
                }
            }
        }

        if (processoReady && creditoZero) {
            atualizaCredito();
        }

    }

    public void atualizaCredito() {
        System.out.println("o-> Atualizando os creditos de todos os Processos");
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() != Processo.Estado.EXIT) {
                if(listaProcessos.get(i).getPrioridade()>0){
                    int credito = listaProcessos.get(i).getCredito() + listaProcessos.get(i).getPrioridade();
                    listaProcessos.get(i).setCredito(credito);
                }else{
                    int credito = (listaProcessos.get(i).getCredito() / 2) + 1;
                    listaProcessos.get(i).setCredito(credito);
                }
                
            }
        }
    }

    public void mudarOrdemTodosProcessos(Processo processo) {
        System.out.println("o-> Mudando a ordem de todos os Processos");
        listaProcessos.get(listaProcessos.indexOf(processo)).setOrdem(listaProcessos.size()); // fica em ultimo da

        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.indexOf(processo) != i) {
                listaProcessos.get(i).setOrdem(listaProcessos.get(i).getOrdem() - 1);
            }
        }
    }

    public boolean existeProcessoRunning() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() == Processo.Estado.RUNNING) {
                return true; 
            }
        }
        return false; 
    }

    public boolean existeProcessoReadyCreditoMaior() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() == Processo.Estado.READY && processo.getCredito() > 0) {
                return true; 
            }
        }
        return false; 
    }

    public boolean todosProcessosFinalizados() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() != Processo.Estado.EXIT) {
                return false; 
            }
        }
        return true; 
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