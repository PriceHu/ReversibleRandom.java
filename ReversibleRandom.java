import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Reversible Random Number Generator
 * <p>
 * This random generator is based on Linear Congruence Algorithm.
 * Original implementation in JavaScript: https://github.com/LovEveRv/reversible-random.js
 */
public class ReversibleRandom {
    private final long a;
    private final long c;
    private final long m;
    private final long inv_a;

    private long current;

    public ReversibleRandom() {
        this(48271, 0, 2147483647);
    }

    /**
     * If you manually provide inv_a, make sure IT IS the inverses of a (mod m).
     * Also make sure a and m are coprime numbers.
     * Otherwise, there will not be a certain inv_a, thus this RNG cannot work properly.
     */
    public ReversibleRandom(long a, long c, long m) {
        this(a, c, m, null);
    }

    /**
     * If you manually provide inv_a, make sure IT IS the inverses of a (mod m).
     * Also make sure a and m are coprime numbers.
     * Otherwise, there will not be a certain inv_a, thus this RNG cannot work properly.
     */
    public ReversibleRandom(long a, long c, long m, Long inv_a) {
        this.a = a;
        this.c = c;
        this.m = m;
        if (inv_a != null) {
            this.inv_a = inv_a;
            if (this.a * this.inv_a % this.m != 1) {
                throw new IllegalArgumentException("The provided inv_a is not the inverses of a (mod m)!");
            }
        } else {
            this.inv_a = getInverse(a, m);
            if (this.a * this.inv_a % this.m != 1) {
                throw new IllegalArgumentException("The provided a and m are not coprime!");
            }
        }
        this.current = ThreadLocalRandom.current().nextLong(m);
    }

    /**
     * Reset the initial value of this RNG. This is equal to something like {@code rand = new ReversibleRandom()},
     * except that you don't need to create another instance.
     */
    public void reset() {
        current = ThreadLocalRandom.current().nextLong(m);
    }

    /**
     * Set initial number between 0 (inclusive) and {@param m} (exclusive).
     */
    public void setInitial(long i) {
        if (i < 0 || i >= m) {
            throw new IllegalArgumentException("Initial number i exceeds [0, RAND_MAX]!");
        }
        current = i;
    }

    /**
     * Set initial number between 0 (inclusive) and {@param bound} (exclusive).
     */
    public void setInitial(long i, long bound) {
        if (i < 0 || i >= bound) {
            throw new IllegalArgumentException("Initial number i exceeds [0, bound)!");
        }
        setInitial(i, 0, bound);
    }

    /**
     * Set initial number between {@param min} (inclusive) and {@param max} (exclusive).
     */
    public void setInitial(long i, long min, long max) {
        if (i < min || i >= max) {
            throw new IllegalArgumentException("Initial number i exceeds [min, max)!");
        }
        long length = max - min;
        long rand = ThreadLocalRandom.current().nextLong(m / length);
        current = rand * length + i - min;
    }

    /**
     * Get next pseudorandom Long between 0 (inclusive) and {@param m} (exclusive).
     *
     * @return next pseudorandom Long between 0 (inclusive) and {@param m} (exclusive).
     */
    public long next() {
        return next(0, m);
    }

    /**
     * Get next pseudorandom Long between 0 (inclusive) and {@param bound} (exclusive).
     *
     * @return next pseudorandom Long between 0 (inclusive) and {@param bound} (exclusive).
     */
    public long next(long bound) {
        return next(0, bound);
    }

    /**
     * Get next pseudorandom Long between {@param min} (inclusive) and {@param max} (exclusive).
     *
     * @return next pseudorandom Long between {@param min} (inclusive) and {@param max} (exclusive).
     */
    public long next(long min, long max) {
        current = (current * a + c) % m;
        return current(min, max);
    }

    /**
     * Get previous pseudorandom Long between 0 (inclusive) and {@param m} (exclusive).
     *
     * @return previous pseudorandom Long between 0 (inclusive) and {@param m} (exclusive).
     */
    public long previous() {
        return previous(0, m);
    }

    /**
     * Get previous pseudorandom Long between 0 (inclusive) and {@param bound} (exclusive).
     *
     * @return previous pseudorandom Long between 0 (inclusive) and {@param bound} (exclusive).
     */
    public long previous(long bound) {
        return previous(0, bound);
    }

    /**
     * Get previous pseudorandom Long between {@param min} (inclusive) and {@param max} (exclusive).
     *
     * @return previous pseudorandom Long between {@param min} (inclusive) and {@param max} (exclusive).
     */
    public long previous(long min, long max) {
        current = ((current + m - c) % m) * inv_a % m;
        return current(min, max);
    }

    /**
     * Get current pseudorandom Long between 0 (inclusive) and {@param m} (exclusive).
     *
     * @return current pseudorandom Long between 0 (inclusive) and {@param m} (exclusive).
     */
    public long current() {
        return current(0, m);
    }

    /**
     * Get current pseudorandom Long between 0 (inclusive) and {@param bound} (exclusive).
     *
     * @return current pseudorandom Long between 0 (inclusive) and {@param bound} (exclusive).
     */
    public long current(long bound) {
        return current(0, bound);
    }

    /**
     * Get current pseudorandom Long between {@param min} (inclusive) and {@param max} (exclusive).
     *
     * @return current pseudorandom Long between {@param min} (inclusive) and {@param max} (exclusive).
     */
    public long current(long min, long max) {
        return current % (max - min) + min;
    }

    /**
     * Get the maximum number that this RNG can generate. This is m - 1.
     *
     * @return the maximum number that this RNG can generate.
     */
    public long getMaxRandom() {
        return m - 1;
    }

    /**
     * Find the inverses of {@param a} (mod {@param n}) using Extended Euclidean Algorithm.
     * That is, find x s.t. {@param a} * x % {@param n} = 1.
     *
     * @return the inverses of {@param a} (mod {@param n}).
     */
    private long getInverse(long a, long n) {
        // Extended Euclidean Algorithm
        List<Long> Q = new ArrayList<Long>();
        while (n != 0) {
            Q.add(a / n);
            long tmp = n;
            n = a % n;
            a = tmp;
        }
        // gcd(a, b) = a
        long x = 1;
        long y = 0;
        while (!Q.isEmpty()) {
            long q = Q.remove(Q.size() - 1);
            long tmp = a;
            a = a * q + n;
            n = tmp;
            tmp = y;
            y = (x - (a / n) * y);
            x = tmp;
        }

        // now we have x and y and original n
        return (x % n + n) % n;
    }
}
