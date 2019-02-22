public enum Operacao {

    UMF("1F"),
    CREDITO("112"),
    CONFIRMACAO("80");

    private String operacao;

    Operacao(String operacao) {
        this.operacao = operacao;
    }

    public String getOperacao() {
        return operacao;
    }

}
