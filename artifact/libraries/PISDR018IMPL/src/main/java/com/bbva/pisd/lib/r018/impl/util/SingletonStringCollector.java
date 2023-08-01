package com.bbva.pisd.lib.r018.impl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;

public class SingletonStringCollector implements Collector<String, List<String>, String> {
    @Override
    public Supplier<List<String>> supplier() { return ArrayList::new; }

    @Override
    public BiConsumer<List<String>, String> accumulator() { return List::add; }

    @Override
    public BinaryOperator<List<String>> combiner() {
        return (identity, container) -> {
            identity.addAll(container);
            return identity;
        };
    }

    @Override
    public Function<List<String>, String> finisher() { return identity -> !isEmpty(identity) ? identity.get(0) : null; }

    @Override
    public Set<Characteristics> characteristics() { return Stream.of(Characteristics.UNORDERED).collect(toSet()); }

}
