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
        p2.setSurtoCPU(3);
        p2.setTempoES(10);
        p2.setTempoTotal(6);
        p2.setOrdem(2);
        p2.setPrioridade(3);

        Processo p3 = new Processo();
        p3.setNomeProcesso(3);
        p3.setSurtoCPU(0);
        p3.setTempoES(0);
        p3.setTempoTotal(14);
        p3.setOrdem(3);
        p3.setPrioridade(3);

        Processo p4 = new Processo();
        p4.setNomeProcesso(4);
        p4.setSurtoCPU(0);
        p4.setTempoES(0);
        p4.setTempoTotal(10);
        p4.setOrdem(4);
        p4.setPrioridade(3);

        // inicializando o tempo de cpu
        tempoGlobal = 0;

        addProcesso(p1);
        addProcesso(p2);
        addProcesso(p3);
        addProcesso(p4);

        escalonarProcesso();

    }

    public void escalonarProcesso() {

        while (!todosProcessosFinalizados()) {// enquanto houver processos na lista

          //  try {
          //      Thread.sleep(100); // Sleep for 1 second
          //  } catch (InterruptedException e) {
          //      e.printStackTrace();
          //  }
            // olha se as condicoes para trocar processo running sao verdadeiras( se nao tem
            // processo running e se tem processo ready com credito maior que 0)
            if (!existeProcessoRunning() && existeProcessoReadyCreditoMaior()) {
                trocaProcessoRunning(); // troca de contexto
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
                    trocaProcessoRunning(); // acha um processo para rodar
                    executaProcesso(); // roda o processo
                }
            }

            if (existeProcessoBlocked()) {
                voltaBloqueio();// verifica se tem processos bloqueados para voltar para a fila de ready
            }

            // if (!existeProcessoRunning()) {
            // trocaProcessoRunning(); // troca de contexto

            // }

            tempoGlobal++;// todo ciclo ele aumenta o tempo de cpu
            System.out.println("\nFINAL -------------------------------------------------");
            System.out.println("Tempo Global: " + tempoGlobal);
            listaProcessos.forEach(processo -> System.out.println("Processo " + processo.getNomeProcesso() +
                    " -> Estado: " + processo.getEstado() +
                    " ->  Credito: " + processo.getCredito() +
                    " ->  Ordem: " + processo.getOrdem() +
                    "  -> Surto CPU atual: " + processo.getSurtoCPUAtual() +
                    "  -> tempo es atual: " + processo.getTempoESatual() +
                    "  -> surtocpu: " + processo.getSurtoCPU() +
                    "  -> tempo es: " + processo.getTempoES() +
                    "  -> Tempo Total: " + processo.getTempoTotal()));
            System.out.println("FINAL -------------------------------------------------\n");
        }

    }

    // OK
    // Só pode ser chamado se existir pelo menos 1 processo que etsá no estado READY
    // com credito maior que 0, sei que não tem ningum em running
    public void trocaProcessoRunning() {

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
        System.out.println("Processo " + listaProcessos.get(indice).getNomeProcesso() + " escolhido");
        listaProcessos.get(indice).setEstado(Processo.Estado.RUNNING);
    }

    public void verificarRunning() {
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() == Processo.Estado.RUNNING) {
                if (listaProcessos.get(i).getTempoTotal() == 0) {
                    System.out.println("Processo " + listaProcessos.get(i).getNomeProcesso() + " finalizado");
                    listaProcessos.get(i).setEstado(Processo.Estado.EXIT);
                    // mudarOrdemTodosProcessos(listaProcessos.get(i));
                    todosProcessosCreditoZero(); // verifica se todos os processos da fila de ready estão com credito 0

                    return;
                }

                if (listaProcessos.get(i).getSurtoCPUAtual() == 0 && listaProcessos.get(i).getTempoES() > 0) {
                    listaProcessos.get(i).setEstado(Processo.Estado.BLOCKED);
                    System.out.println(
                            "Processo " + listaProcessos.get(i).getNomeProcesso() + " foi para fila de bloqueado");
                    listaProcessos.get(i).setSurtoCPUAtual(listaProcessos.get(i).getSurtoCPU());
                    // mudarOrdemTodosProcessos(listaProcessos.get(i));
                    todosProcessosCreditoZero(); // verifica se todos os processos da fila de ready estão com credito 0

                    return;
                }

                if (listaProcessos.get(i).getCredito() == 0) {
                    System.out.println("Processo " + listaProcessos.get(i).getNomeProcesso()
                            + " voltando para a fila de ready");
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
                return;
            }
        }

    }

    public void voltaBloqueio() {

        for (int i = 0; i < listaProcessos.size(); i++) {

            if (listaProcessos.get(i).getEstado() == Processo.Estado.BLOCKED) {

                if (listaProcessos.get(i).getTempoESatual() == 0) {
                    System.out.println("Processo " + listaProcessos.get(i).getNomeProcesso() + " desbloqueado");
                    listaProcessos.get(i).setEstado(Processo.Estado.READY);
                    mudarOrdemTodosProcessos(listaProcessos.get(i));
                    listaProcessos.get(i).setTempoESatual(listaProcessos.get(i).getTempoES());

                } else {
                    listaProcessos.get(i).setTempoESatual(listaProcessos.get(i).getTempoESatual() - 1);

                    System.out.println("Tempo de E/S: " + listaProcessos.get(i).getTempoESatual() + " do processo "
                            + listaProcessos.get(i).getNomeProcesso());
                }
            }
        }

    }

    public void todosProcessosCreditoZero() {

        for (int i = 0; i < listaProcessos.size(); i++) {
            Processo processo = listaProcessos.get(i);
            if (processo.getEstado() == Processo.Estado.READY) {
                if (processo.getCredito() > 0) {
                    // System.out.println("Ainda tem um Processo com credito maior que 0 no estado
                    // READY");
                    return;
                }
            }
        }

        atualizaCredito(); // se todos os processos da fila de ready estiverem com credito 0, atualiza o
                           // credito
    }

    public void atualizaCredito() {
        System.out.println("Atualizando creditos");
        for (int i = 0; i < listaProcessos.size(); i++) {
            if (listaProcessos.get(i).getEstado() != Processo.Estado.EXIT) {
                int credito = (listaProcessos.get(i).getCredito() / 2) + listaProcessos.get(i).getPrioridade();
                listaProcessos.get(i).setCredito(credito);
            }

        }
    }

    // aqui mexer quando nao quiser aumentar creditos de processos bloqueados pois
    // isso nao deixa o exemplo igual

    public void mudarOrdemTodosProcessos(Processo processo) {

        listaProcessos.get(listaProcessos.indexOf(processo)).setOrdem(listaProcessos.size() - 1); // fica em ultimo da
                                                                                                  // ordem

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