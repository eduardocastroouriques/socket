public class OperacaoUtil {

    public static String get(String message){

        String operacao = message.substring(0, 2);

        switch (operacao){
            case "1F":
                return Operacao.UMF.toString();
            case "12":
                return Operacao.CREDITO.toString();
            case "80":
                return Operacao.CONFIRMACAO.toString();
            default:
                return null;
        }
    }
}
