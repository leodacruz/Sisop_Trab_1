package sisop_trab_1;

public class Processo {
    private Estado estado;
    private int nomeProcesso;
    private int surtoCPU;
    private int surtoCPUAtual;
    
    private int tempoES;
    private int tempoESatual;
    
    private int tempoTotal;
    private int ordem;
    private int prioridade;
    private int credito;

    public Processo() {
        this.estado = Estado.READY;
        this.credito = 0;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Estado getEstado() {
        return estado;
    }

    public int getNomeProcesso() {
        return nomeProcesso;
    }

    public void setNomeProcesso(int nomeProcesso) {
        this.nomeProcesso = nomeProcesso;
    }

    public int getSurtoCPU() {
        return surtoCPU;
    }

    public void setSurtoCPU(int surtoCPU) {
        this.surtoCPU = surtoCPU;
        this.surtoCPUAtual = surtoCPU;
    }

    public int getTempoES() {
        return tempoES;
    }

    public void setTempoES(int tempoES) {
        this.tempoES = tempoES;
        this.tempoESatual = tempoES;
    }

    public int getTempoTotal() {
        return tempoTotal;
    }

    public void setTempoTotal(int tempoTotal) {
        this.tempoTotal = tempoTotal;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
        this.credito = prioridade;
    }

    public int getCredito() {
        return credito;
    }

    public void setCredito(int creditos) {
        this.credito = creditos;
    }

    public enum Estado {
        READY, RUNNING, BLOCKED, EXIT
    }

    public int getTempoESatual() {
        return tempoESatual;
    }

    public void setTempoESatual(int tempoESatual) {
        this.tempoESatual = tempoESatual;
    }

    public int getSurtoCPUAtual() {
        return surtoCPUAtual;
    }

    public void setSurtoCPUAtual(int surtoCPUAtual) {
        this.surtoCPUAtual = surtoCPUAtual;
    }

}
