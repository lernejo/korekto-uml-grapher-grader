package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.toolkit.misc.RandomSupplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

class IdentifierGeneratorTest {

    @ParameterizedTest
    @CsvSource({
        "10, true, FPgNOyIF",
        "12, false, milpVwhOgdH"
    })
    public void deterministic_id_gen(int seed, boolean upper, String expectedResult) {
        Random random = new Random(seed);
        RandomSupplier rs = random::nextInt;

        String result = new IdentifierGenerator(rs).generateId(upper);
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }
}
