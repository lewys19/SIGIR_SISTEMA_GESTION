package pe.sigir.compartido;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> items, long total, int pagina, int tamano) {
    public <R> Pagina<R> map(Function<T, R> f) {
        return new Pagina<>(items.stream().map(f).toList(), total, pagina, tamano);
    }
}
