package sisop_trab_1;

public class Processo {

    private Estado estado;
    private String nomeProcesso;
    private int surtoCPU;
    private int surtoCPUAtual;

    private int tempoES;
    private int tempoESatual;

    private int tempoCPU;

    private boolean executado;
    private int turnaroundTime;

    private boolean response;
    private int responseTime;

    private int tempoTotal;
    private int ordem;
    private int prioridade;

    private int credito;

    private int waitTime;

    public Processo() {
        this.estado = Estado.READY;
        this.credito = 0;
        this.turnaroundTime = 0;
        this.tempoCPU = 0;
        this.response = false;
        this.executado = true; // pq ja comecam na fila de ready
        this.responseTime = -1;
        this.waitTime = 0;
    }

    public int getTempoCPU() {
        return tempoCPU;
    }

    public boolean isExecutado() {
        return executado;
    }

    public void setExecutado(boolean executado) {
        this.executado = executado;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int tempoExecutado) {
        this.turnaroundTime = tempoExecutado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getNomeProcesso() {
        return nomeProcesso;
    }

    public void setNomeProcesso(String nomeProcesso) {
        this.nomeProcesso = nomeProcesso;
    }

    public int getSurtoCPU() {
        return surtoCPU;
    }

    public void setSurtoCPU(int surtoCPU) {
        this.surtoCPU = surtoCPU;
        this.surtoCPUAtual = surtoCPU;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
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

    public void setTempoCPU(int tempoCPU) {
        this.tempoCPU = tempoCPU;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

}
