package sisop_trab_1;

import java.util.ArrayList;
import java.util.List;

public class Escalonador {

    private List<Processo> listaProcessos = new ArrayList<Processo>();
    private int tempoGlobal;

    public void addProcesso(Processo processo) {
        listaProcessos.add(processo);
    }

    public void removeProcesso(Processo processo) {
        listaProcessos.remove(processo);
    }

    public void execucaoEscalonar() {
        // criando processos de teste
        Processo p1 = new Processo();
        p1.setNomeProcesso(1);
        p1.setSurtoCPU(2);
        p1.setTempoES(5);
        p1.setTempoTotal(6);
        p1.setOrdem(1);
        p1.setPrioridade(3);

        Processo p2 = new Processo();
        p2.setNomeProcesso(2);
        p2.setSurtoCPU(0);
        p2.setTempoES(0);
        p2.setTempoTotal(10);
        p2.setOrdem(2);
        p2.setPrioridade(4);

        // inicializando o tempo de cpu
        tempoGlobal = 0;

        addProcesso(p1);
        // addProcesso(p2);

        escalonarProcesso();

    }

    public void escalonarProcesso() {

        while (!todosProcessosFinalizados()) {// enquanto houver processos na lista
            System.out.println("\nTempo Global: " + tempoGlobal);

            voltaBloqueio();// verifica se tem processos bloqueados para voltar para a fila de ready

            todosProcessosCreditoZero(); // verifica se todos os processos estão com credito 0 para atualizar o credito

            trocaContexto(); // troca de contexto

            executaProcesso();

            tempoGlobal++;// todo ciclo ele aumenta o tempo de cpu

        }

    }

    public void voltaBloqueio() {

        if (!existeProcessoBlocked()) {
            return;
        }

        System.out.println("Verificando processos bloqueados");

        for (int i = 0; i < listaProcessos.size(); i++) {

            if (listaProcessos.get(i).getEstado() == Processo.Estado.BLOCKED) {
                
                if (listaProcessos.get(i).getTempoESatual() == 0) {
                    System.out.println("Processo " + listaProcessos.get(i).getNomeProcesso() + " desbloqueado");
                    listaProcessos.get(i).setEstado(Processo.Estado.READY);
                    listaProcessos.get(i).setTempoESatual(listaProcessos.get(i).getTempoES());

                } else {
                    listaProcessos.get(i).setTempoES(listaProcessos.get(i).getTempoESatual() - 1);
                    System.out.println("Tempo de E/S: " + listaProcessos.get(i).getTempoESatual() + " do processo "
                            + listaProcessos.get(i).getNomeProcesso());
                }
            }
        }

    }

    public void todosProcessosCreditoZero() {
        if (existeProcessoRunning()) {
            return;
        }

        for (int i = 0; i < listaProcessos.size(); i++) {
            Processo processo = listaProcessos.get(i);
            if (processo.getEstado() == Processo.Estado.READY) {
                if (processo.getCredito() > 0) {
                    System.out.println("Ainda tem um Processo com credito maior que 0 no estado READY");
                    return;
                }
            }
        }

        atualizaCredito(); // se todos os processos da fila de ready estiverem com credito 0, atualiza o
                           // credito
    }

    // aqui mexer quando nao quiser aumentar creditos de processos bloqueados pois
    // isso nao deixa o exemplo igual
    public void atualizaCredito() {
        System.out.println("Atualizando creditos");
        for (int i = 0; i < listaProcessos.size(); i++) {
            int credito = (listaProcessos.get(i).getCredito() / 2) + listaProcessos.get(i).getPrioridade();
            listaProcessos.get(i).setCredito(credito);
        }
    }

    // quando for pensqar em prioridade e ordem é aqui que é pra mexer
    public void trocaContexto() {

        if (existeProcessoRunning()) {
            return;
        }

        int creditoMax = 0;
        int indice = 0;

        for (int i = 0; i < listaProcessos.size(); i++) {// achando o processo com maior prioridade

            Processo processo = listaProcessos.get(i);

            if (processo.getEstado() == Processo.Estado.READY) {
                int credito = processo.getCredito();

                if (credito > creditoMax) {
                    creditoMax = credito;
                    indice = i;
                }
            }

        }
        if (listaProcessos.get(indice).getEstado() == Processo.Estado.BLOCKED) {
            return;
        }
        System.out.println("Processo " + listaProcessos.get(indice).getNomeProcesso() + " escolhido");
        listaProcessos.get(indice).setEstado(Processo.Estado.RUNNING);

    }

    public void executaProcesso() {
        if (existeProcessoRunning()) {

            for (int i = 0; i < listaProcessos.size(); i++) {
                if (listaProcessos.get(i).getEstado() == Processo.Estado.RUNNING) {// se o processo estiver em execução

                    System.out.println("Processo " + listaProcessos.get(i).getNomeProcesso() + " em execução");
                    System.out.println("Surto de CPU: " + listaProcessos.get(i).getSurtoCPU());
                    System.out.println("Tempo Total: " + listaProcessos.get(i).getTempoTotal());
                    System.out.println("Credito: " + listaProcessos.get(i).getCredito());

                    if (listaProcessos.get(i).getSurtoCPUAtual() == 0 && listaProcessos.get(i).getTempoES() > 0) {
                        listaProcessos.get(i).setEstado(Processo.Estado.BLOCKED);
                        System.out.println(
                                "Processo " + listaProcessos.get(i).getNomeProcesso() + " foi para fila de bloqueado");
                        listaProcessos.get(i).setSurtoCPUAtual(listaProcessos.get(i).getSurtoCPU());
                        return;
                    }

                    if (listaProcessos.get(i).getCredito() == 0) {
                        System.out.println("Processo " + listaProcessos.get(i).getNomeProcesso()
                                + " voltando para a fila de ready");
                        listaProcessos.get(i).setEstado(Processo.Estado.READY);
                        return;
                    }

                    if (listaProcessos.get(i).getTempoTotal() == 0) {
                        System.out.println("Processo " + listaProcessos.get(i).getNomeProcesso() + " finalizado");
                        listaProcessos.get(i).setEstado(Processo.Estado.EXIT);
                        return;
                    }

                    listaProcessos.get(i).setSurtoCPUAtual(listaProcessos.get(i).getSurtoCPUAtual() - 1);
                    listaProcessos.get(i).setTempoTotal(listaProcessos.get(i).getTempoTotal() - 1);
                    listaProcessos.get(i).setCredito(listaProcessos.get(i).getCredito() - 1);

                    return;

                }

            }

        } else {
            System.out.println("Nenhum processo em estado RUNNING");
            return;
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

    public boolean existeProcessoBlocked() {
        for (Processo processo : listaProcessos) {
            if (processo.getEstado() == Processo.Estado.BLOCKED) {
                System.out.println("Processo " + processo.getNomeProcesso() + " bloqueado");
                return true; // Encontrou pelo menos um processo em estado BLOCKED
            }
        }
        System.out.println("Nenhum processo bloqueado");
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

}