import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    // CT01 - RF01 - produto válido
    @Test
    void deveSalvarProdutoComDadosValidos() {
        // Arrange
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(repository.salvar(produto)).thenReturn(produto);


        Produto resultado = service.salvar(produto);


        assertEquals("Mouse", resultado.getNome());
        assertEquals(49.90, resultado.getPreco(), 0.001);
        assertEquals(10, resultado.getQuantidade());
        verify(repository).salvar(produto);
    }

    // CT02 - RF02 - preço negativo
    @Test
    void naoDeveSalvarProdutoComPrecoNegativo() {
        Produto produto = new Produto("Mouse", -10, 5);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verify(repository, never()).salvar(any());
    }

    // CT03 / CT11 - RF02 - preço igual a zero
    @Test
    void naoDeveSalvarProdutoComPrecoIgualAZero() {
        Produto produto = new Produto("Mouse", 0, 5);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> service.salvar(produto));

        assertEquals("Preço deve ser maior que zero", excecao.getMessage());
        verify(repository, never()).salvar(any());
    }


    @Test
    void naoDeveSalvarProdutoComNomeVazio() {
        Produto produto = new Produto("", 49.90, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verify(repository, never()).salvar(any());
    }


    @Test
    void naoDeveSalvarProdutoComNomeNulo() {
        Produto produto = new Produto(null, 49.90, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verify(repository, never()).salvar(any());
    }


    @Test
    void naoDeveSalvarProdutoComQuantidadeNegativa() {
        Produto produto = new Produto("Mouse", 49.90, -1);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verify(repository, never()).salvar(any());
    }


    @Test
    void deveSalvarProdutoComQuantidadeIgualAZero() {
        Produto produto = new Produto("Mouse", 49.90, 0);
        when(repository.salvar(produto)).thenReturn(produto);

        Produto resultado = service.salvar(produto);

        assertEquals(0, resultado.getQuantidade());
        verify(repository).salvar(produto);
    }


    @Test
    void deveRetornarProdutoQuandoEncontrado() {
        Produto produto = new Produto("Mouse", 49.90, 10);
        when(repository.buscarPorId(1L)).thenReturn(produto);

        Produto resultado = service.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("Mouse", resultado.getNome());
        verify(repository).buscarPorId(1L);
    }

    // CT09 / CT14 - RF03 - buscar produto inexistente
    @Test
    void deveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.buscarPorId(99L)).thenReturn(null);

        assertThrows(ProdutoNaoEncontradoException.class,
                () -> service.buscarPorId(99L));

        verify(repository).buscarPorId(99L);
    }

    // CT10 / CT15 - RF04 - falha de conexão ao salvar
    @Test
    void deveLancarErroQuandoRepositoryFalharAoSalvar() {
        Produto produto = new Produto("Teclado", 129.90, 5);
        doThrow(new RuntimeException("Falha na conexão"))
                .when(repository).salvar(any(Produto.class));

        ErroAoSalvarProdutoException excecao = assertThrows(
                ErroAoSalvarProdutoException.class,
                () -> service.salvar(produto));

        assertEquals("Falha ao salvar o produto", excecao.getMessage());
        verify(repository, times(1)).salvar(produto);
    }



    // CT12 - RF02 - preço 0.01
    @Test
    void deveSalvarProdutoComPrecoMinimoValido() {
        Produto produto = new Produto("Bala", 0.01, 10);
        when(repository.salvar(produto)).thenReturn(produto);

        Produto resultado = service.salvar(produto);

        assertEquals(0.01, resultado.getPreco(), 0.001);
        verify(repository).salvar(produto);
    }

    // CT13 - RF02 - nome em branco
    @Test
    void naoDeveSalvarProdutoComNomeEmBranco() {
        Produto produto = new Produto("   ", 49.90, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(produto));

        verify(repository, never()).salvar(any());
    }

    // Extra - RF02 - produto nulo
    @Test
    void naoDeveSalvarProdutoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.salvar(null));

        verify(repository, never()).salvar(any());
    }
}
