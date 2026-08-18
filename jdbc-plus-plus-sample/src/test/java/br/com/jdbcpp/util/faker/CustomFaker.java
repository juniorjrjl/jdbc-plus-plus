package br.com.jdbcpp.util.faker;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
public class CustomFaker extends Faker {

    private static final Logger log = LoggerFactory.getLogger(CustomFaker.class);

    private static CustomFaker customFaker = null;
    private static long initialSeed;

    private CustomFaker(final long seed) {
        super(new Random(seed));
    }

    private static long getSeed(){
        final var seed = System.getProperty("test.seed");
        initialSeed = (nonNull(seed) && !seed.isBlank())
                ? Long.parseLong(seed)
                : new Random().nextLong();
        return initialSeed;
    }

    public static CustomFaker getInstance() {
        if (isNull(customFaker)) {
            initialSeed = getSeed();

            log.info("****************************************************");
            log.info("Execution Seed: {}", initialSeed);
            log.info("To repeat this exact data, use: -Dtest.seed={}", initialSeed);
            log.info("****************************************************");

            customFaker = new CustomFaker(initialSeed);
        }
        return customFaker;
    }

    public void reseed() {
        log.debug("Resetting Faker instance to initial seed...");
        customFaker = new CustomFaker(initialSeed);
    }

    public NumericProvider numeric(){
        return getProvider(NumericProvider.class, NumericProvider::new);
    }

    @SafeVarargs
    public final <E extends Enum<E>> E option(final Class<E> enumeration, final E... exceptedValues) {
        final var options = enumeration.getEnumConstants();
        final var expectedSet = new HashSet<>(Arrays.asList(exceptedValues));
        if (expectedSet.size() == options.length){
            throw new IllegalArgumentException("All elements in 'exceptedValues'");
        }
        final var values = new ArrayList<>(List.of(options));
        values.removeAll(expectedSet);
        return values.get(customFaker.random().nextInt(values.size()));
    }

}
