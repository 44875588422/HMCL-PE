package com.tungsten.hmclpe.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.tungsten.hmclpe.utils.function.ExceptionalBiConsumer;
import com.tungsten.hmclpe.utils.function.ExceptionalConsumer;
import com.tungsten.hmclpe.utils.function.ExceptionalFunction;
import com.tungsten.hmclpe.utils.function.ExceptionalRunnable;
import com.tungsten.hmclpe.utils.function.ExceptionalSupplier;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Stream;

/**
 *
 * @author huangyuhui
 */
public final class Lang {

    private Lang() {
    }

    /**
     * Construct a mutable map by given key-value pairs.
     * @param pairs entries in the new map
     * @param <K> the type of keys
     * @param <V> the type of values
     * @return the map which contains data in {@code pairs}.
     */
    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Pair<K, V>... pairs) {
        return mapOf(Arrays.asList(pairs));
    }

    /**
     * Construct a mutable map by given key-value pairs.
     * @param pairs entries in the new map
     * @param <K> the type of keys
     * @param <V> the type of values
     * @return the map which contains data in {@code pairs}.
     */
    public static <K, V> Map<K, V> mapOf(Iterable<Pair<K, V>> pairs) {
        Map<K, V> map = new LinkedHashMap<>();
        for (Pair<K, V> pair : pairs)
            map.put(pair.getKey(), pair.getValue());
        return map;
    }

    @SafeVarargs
    public static <T> List<T> immutableListOf(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    public static <T extends Comparable<T>> T clamp(T min, T val, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

    public static double clamp(double min, double val, double max) {
        return Math.max(min, Math.min(val, max));
    }

    public static int clamp(int min, int val, int max) {
        return Math.max(min, Math.min(val, max));
    }

    public static boolean test(ExceptionalRunnable<?> r) {
        try {
            r.run();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <E extends Exception> boolean test(ExceptionalSupplier<Boolean, E> r) {
        try {
            return r.get();
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> T ignoringException(ExceptionalSupplier<T, ?> supplier) {
        return ignoringException(supplier, null);
    }

    public static <T> T ignoringException(ExceptionalSupplier<T, ?> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception ignore) {
            return defaultValue;
        }
    }

    /**
     * Cast {@code obj} to V dynamically.
     * @param obj the object reference to be cast.
     * @param clazz the class reference of {@code V}.
     * @param <V> the type that {@code obj} is being cast to.
     * @return {@code obj} in the type of {@code V}.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <V> Optional<V> tryCast(Object obj, Class<V> clazz) {
        if (clazz.isInstance(obj)) {
            return Optional.of(clazz.cast(obj));
        } else {
            return Optional.empty();
        }
    }

    public static <T> T getOrDefault(List<T> a, int index, T defaultValue) {
        return index < 0 || index >= a.size() ? defaultValue : a.get(index);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T> T merge(T a, T b, BinaryOperator<T> operator) {
        if (a == null) return b;
        if (b == null) return a;
        return operator.apply(a, b);
    }

    public static <T> List<T> removingDuplicates(List<T> list) {
        LinkedHashSet<T> set = new LinkedHashSet<>(list.size());
        set.addAll(list);
        return new ArrayList<>(set);
    }

    /**
     * Join two collections into one list.
     *
     * @param a one collection, to be joined.
     * @param b another collection to be joined.
     * @param <T> the super type of elements in {@code a} and {@code b}
     * @return the joint collection
     */
    public static <T> List<T> merge(Collection<? extends T> a, Collection<? extends T> b) {
        List<T> result = new ArrayList<>();
        if (a != null)
            result.addAll(a);
        if (b != null)
            result.addAll(b);
        return result;
    }

    public static <T> List<T> copyList(List<T> list) {
        return list == null ? null : list.isEmpty() ? null : new ArrayList<>(list);
    }

    public static void executeDelayed(Runnable runnable, TimeUnit timeUnit, long timeout, boolean isDaemon) {
        thread(() -> {
            try {
                timeUnit.sleep(timeout);
                runnable.run();
            } catch (InterruptedException ignore) {
            }

        }, null, isDaemon);
    }

    /**
     * Start a thread invoking {@code runnable} immediately.
     * @param runnable code to run.
     * @return the reference of the started thread
     */
    public static Thread thread(Runnable runnable) {
        return thread(runnable, null);
    }

    /**
     * Start a thread invoking {@code runnable} immediately.
     * @param runnable code to run
     * @param name the name of thread
     * @return the reference of the started thread
     */
    public static Thread thread(Runnable runnable, String name) {
        return thread(runnable, name, false);
    }

    /**
     * Start a thread invoking {@code runnable} immediately.
     * @param runnable code to run
     * @param name the name of thread
     * @param isDaemon true if thread will be terminated when only daemon threads are running.
     * @return the reference of the started thread
     */
    public static Thread thread(Runnable runnable, String name, boolean isDaemon) {
        Thread thread = new Thread(runnable);
        if (isDaemon)
            thread.setDaemon(true);
        if (name != null)
            thread.setName(name);
        thread.start();
        return thread;
    }

    public static ThreadPoolExecutor threadPool(String name, boolean daemon, int threads, long timeout, TimeUnit timeunit) {
        AtomicInteger counter = new AtomicInteger(1);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, timeout, timeunit, new LinkedBlockingQueue<>(), r -> {
            Thread t = new Thread(r, name + "-" + counter.getAndIncrement());
            t.setDaemon(daemon);
            return t;
        });
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    public static int parseInt(Object string, int defaultValue) {
        try {
            return Integer.parseInt(string.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer toIntOrNull(Object string) {
        try {
            if (string == null) return null;
            return Integer.parseInt(string.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Find the first non-null reference in given list.
     * @param t nullable references list.
     * @param <T> the type of nullable references
     * @return the first non-null reference.
     */
    @SafeVarargs
    public static <T> T nonNull(T... t) {
        for (T a : t) if (a != null) return a;
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T> T apply(T t, Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void rethrow(Throwable e) {
        if (e == null)
            return;
        if (e instanceof ExecutionException || e instanceof CompletionException) { // including UncheckedException and UncheckedThrowable
            rethrow(e.getCause());
        } else if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new CompletionException(e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Runnable wrap(ExceptionalRunnable<?> runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                rethrow(e);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T> Supplier<T> wrap(ExceptionalSupplier<T, ?> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                rethrow(e);
                throw new InternalError("Unreachable code");
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T, R> Function<T, R> wrap(ExceptionalFunction<T, R, ?> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (Exception e) {
                rethrow(e);
                throw new InternalError("Unreachable code");
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T> Consumer<T> wrapConsumer(ExceptionalConsumer<T, ?> fn) {
        return t -> {
            try {
                fn.accept(t);
            } catch (Exception e) {
                rethrow(e);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T, E> BiConsumer<T, E> wrap(ExceptionalBiConsumer<T, E, ?> fn) {
        return (t, e) -> {
            try {
                fn.accept(t, e);
            } catch (Exception ex) {
                rethrow(ex);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SafeVarargs
    public static <T> Consumer<T> compose(Consumer<T>... consumers) {
        return t -> {
            for (Consumer<T> consumer : consumers) {
                consumer.accept(t);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Stream<T> toStream(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }

    public static <T> Iterable<T> toIterable(Enumeration<T> enumeration) {
        if (enumeration == null) {
            throw new NullPointerException();
        }
        return () -> new Iterator<T>() {
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            public T next() {
                return enumeration.nextElement();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T> Iterable<T> toIterable(Stream<T> stream) {
        return stream::iterator;
    }

    public static <T> Iterable<T> toIterable(Iterator<T> iterator) {
        return () -> iterator;
    }

    /**
     * This is a useful function to prevent exceptions being eaten when using CompletableFuture.
     * You can write:
     * ... .exceptionally(handleUncaught);
     */
    public static final Function<Throwable, Void> handleUncaught = e -> {
        handleUncaughtException(e);
        return null;
    };

    public static <R> R handleUncaughtException(Throwable e) {
        Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        return null;
    }
}
