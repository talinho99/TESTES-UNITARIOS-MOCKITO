import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService service;



    // PED01 - RF05 - pedido válido: confere o valor total
    @Test
    void deveCalcularValorTotalDoPedido() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        Pedido pedido = service.criarPedido(1L, 2);

        assertEquals(99.80, pedido.getValorTotal(), 0.001);
        assertEquals(2, pedido.getQuantidade());
        assertEquals("Mouse", pedido.getProduto().getNome());
    }

    // PED02 - RF05 - pedido válido: confere as interações com os dois repositórios
    @Test
    void deveBuscarProdutoESalvarPedidoUmaVez() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        service.criarPedido(1L, 2);

        verify(produtoRepository, times(1)).buscarPorId(1L);
        verify(pedidoRepository, times(1)).salvar(any(Pedido.class));
    }

    // PED03 + PED08 - quantidade igual a zero
    @Test
    void naoDeveCriarPedidoComQuantidadeIgualAZero() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criarPedido(1L, 0));

        verify(produtoRepository, never()).buscarPorId(any());
        verify(pedidoRepository, never()).salvar(any());
    }

    // PED04 + PED08 - quantidade negativa
    @Test
    void naoDeveCriarPedidoComQuantidadeNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criarPedido(1L, -3));

        verify(produtoRepository, never()).buscarPorId(any());
        verify(pedidoRepository, never()).salvar(any());
    }

    // PED05 + PED08 - produto inexistente
    @Test
    void deveLancarExcecaoQuandoProdutoDoPedidoNaoExistir() {
        when(produtoRepository.buscarPorId(99L)).thenReturn(null);

        assertThrows(ProdutoNaoEncontradoException.class,
                () -> service.criarPedido(99L, 2));

        verify(produtoRepository).buscarPorId(99L);
        verify(pedidoRepository, never()).salvar(any());
    }

    // PED06 + PED08 - quantidade maior que o estoque (11 com estoque 10)
    @Test
    void deveLancarExcecaoQuandoQuantidadeMaiorQueEstoque() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        assertThrows(EstoqueInsuficienteException.class,
                () -> service.criarPedido(1L, 11));

        verify(pedidoRepository, never()).salvar(any());
    }

    // PED07 - quantidade igual ao estoque (fronteira válida)
    @Test
    void deveCriarPedidoComQuantidadeIgualAoEstoque() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);

        Pedido pedido = service.criarPedido(1L, 10);

        assertEquals(499.00, pedido.getValorTotal(), 0.001);
        verify(pedidoRepository, times(1)).salvar(any(Pedido.class));
    }





    @Test
    void deveLancarExcecaoQuandoBancoEstiverForaDoArAoSalvarPedido() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);
        doThrow(new RuntimeException("Falha na conexão"))
                .when(pedidoRepository).salvar(any(Pedido.class));

        assertThrows(RuntimeException.class,
                () -> service.criarPedido(1L, 2));

        verify(pedidoRepository, times(1)).salvar(any(Pedido.class));
    }



    // Cenário C - falha no ProdutoRepository
    @Test
    void naoDeveSalvarPedidoQuandoProdutoRepositoryFalhar() {
        when(produtoRepository.buscarPorId(1L))
                .thenThrow(new RuntimeException("Falha na conexão"));

        assertThrows(RuntimeException.class,
                () -> service.criarPedido(1L, 2));

        verify(pedidoRepository, never()).salvar(any());
    }

    // Cenário D - Service melhorado: falha do PedidoRepository vira exceção própria (correção feita)
    @Test
    void deveLancarErroAoSalvarPedidoQuandoPedidoRepositoryFalhar() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        RuntimeException falha = new RuntimeException("Falha na conexão");
        when(produtoRepository.buscarPorId(1L)).thenReturn(produto);
        doThrow(falha).when(pedidoRepository).salvar(any(Pedido.class));

        ErroAoSalvarPedidoException excecao = assertThrows(
                ErroAoSalvarPedidoException.class,
                () -> service.criarPedido(1L, 2));

        assertEquals("Falha ao salvar o pedido", excecao.getMessage());
        assertSame(falha, excecao.getCause());
        verify(pedidoRepository, times(1)).salvar(any(Pedido.class));
    }
}
