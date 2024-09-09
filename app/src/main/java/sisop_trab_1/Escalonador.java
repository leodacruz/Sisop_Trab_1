package sisop_trab_1;

import java.util.ArrayList;
import java.util.List;

public class Escalonador {

    private List<Processo> listaProcessos = new ArrayList<Processo>();

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
        p1.setSurtoCPU(0);
        p1.setTempoES(0);
        p1.setTempoTotal(10);
        p1.setOrdem(1);
        p1.setPrioridade(3);

        Processo p2 = new Processo();
        p2.setNomeProcesso(2);
        p2.setSurtoCPU(0);
        p2.setTempoES(0);
        p2.setTempoTotal(10);
        p2.setOrdem(2);
        p2.setPrioridade(4);

        addProcesso(p1);
        addProcesso(p2);

        escalonarProcesso();

    }

    public void escalonarProcesso() {
        

        while (!listaProcessos.isEmpty()) {// enquanto houver processos na lista
            int creditoMax = 0;
            int indice = 0;

           // System.out.println("passo- 1");
            todosProcessosCreditoZero(); // verifica se todos os processos estão com credito 0 para atualizar o credito
           // System.out.println("passo -2 ");
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
          //  System.out.println("passo -3 ");
            executaProcesso(listaProcessos.get(indice));
           // System.out.println("passo -4 ");

        }

    }

    public void todosProcessosCreditoZero() {
       // System.out.println("passo 1 -> 1");
        for (int i = 0; i < listaProcessos.size(); i++) {
            Processo processo = listaProcessos.get(i);
            if (processo.getEstado() == Processo.Estado.READY) {
                if (processo.getCredito() != 0) {
                    return;
                }
            }
        }
      //  System.out.println("passo 1 -> 2");
        atualizaCredito(); // se todos os processos da fila de ready estiverem com credito 0, atualiza o
                           // credito
                          // System.out.println("passo 1 -> 3");
    }

    public void atualizaCredito() {
        //System.out.println("passo 1 -> 2 -> 1");
      //  System.out.println("Atualizando o credito dos processos");
        for (int i = 0; i < listaProcessos.size(); i++) {
            int credito = (listaProcessos.get(i).getCredito() / 2) + listaProcessos.get(i).getPrioridade();
            listaProcessos.get(i).setCredito(credito);
        }
      //  System.out.println("passo 1 -> 2 -> 2");
    }

    public void executaProcesso(Processo processo) {
       // System.out.println("Executando processo " + processo.getNomeProcesso());


        processo.setEstado(Processo.Estado.RUNNING);

        //Executando o processo
        while(processo.getEstado() == Processo.Estado.RUNNING){
            System.out.println("Processo " + processo.getNomeProcesso() + " executando e possui " + processo.getCredito()
            + " de credito");
            processo.setCredito(processo.getCredito() - 1);
    
            if (processo.getCredito() == 0) {//acabou os creditos acaba o processo
                processo.setEstado(Processo.Estado.EXIT);
                System.out.println("Processo " + processo.getNomeProcesso() + " Acabou");
                removeProcesso(processo);
            }
        }


    }

}
