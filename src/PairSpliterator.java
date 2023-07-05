import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PairSpliterator<T, R> implements Spliterator<R> {
    Spliterator<T> source;
    boolean hasPrev;
    boolean hasLast;
    private T current;
    private final T last;
    private final BiFunction<T, T, R> mapper;


    public PairSpliterator(BiFunction<T, T, R> mapper, Spliterator<T> source) {
        this(mapper, source, null, false, null, false);
    }

    public PairSpliterator(BiFunction<T, T, R> mapper, Spliterator<T> source, T prev, boolean hasPrev, T last, boolean hasLast) {
        this.mapper = mapper;
        this.source = source;
        this.current = prev;
        this.last = last;
        this.hasPrev = hasPrev;
        this.hasLast = hasLast;
    }

    public void setCurrent(T current) {
        this.current = current;
    }

    @Override
    public int characteristics() {
        return source.characteristics() & (ORDERED | SIZED | SUBSIZED | IMMUTABLE | CONCURRENT);
    }

    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        if(!hasPrev) {
            if(!source.tryAdvance(this::setCurrent)) {
                return false;
            }

            hasPrev = true;
        }

        T prev = current;

        if(!source.tryAdvance(this::setCurrent)) {
            if(!hasLast) {
                return false;
            }

            hasLast = false;
            current = last;
        }

        action.accept(mapper.apply(prev, current));
        return true;
    }

    @Override
    public Spliterator<R> trySplit() {
        Spliterator<T> prefixSource = source.trySplit();
        if(prefixSource == null) {
            return null;
        }

        T prev = current;

        if(!source.tryAdvance(this::setCurrent)) {
            source = prefixSource;
            return null;
        }

        boolean oldHasPrev = hasPrev;
        hasPrev = true;

        return new PairSpliterator<>(mapper, prefixSource, prev, oldHasPrev, current, true);
    }

    @Override
    public long estimateSize() {
        long size = source.estimateSize();

        if(size == Long.MAX_VALUE) {
            return size;
        } else if(hasLast) {
            size++;
        } else if(!hasLast && size > 0) {
            size--;
        }

        return size;
    }
}
