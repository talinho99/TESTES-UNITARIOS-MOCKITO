public class ErroAoSalvarProdutoException extends RuntimeException {

    public ErroAoSalvarProdutoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
