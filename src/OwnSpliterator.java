import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class OwnSpliterator {
    public static <T, R> Stream<R> pairMap(Stream<T> stream, BiFunction<T, T, R> mapper) {
        return StreamSupport.stream(new PairSpliterator<>(mapper, stream.spliterator()), stream.isParallel()).onClose(stream::close);
    }
}
