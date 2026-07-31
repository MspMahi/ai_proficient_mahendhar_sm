package com.mahendhar.urlshortener.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void generatedCodeUsesOnlyUrlSafeAlphaNumericCharacters() {
        String code = generator.generate(24);

        assertThat(code).hasSize(24).matches("[A-Za-z0-9]+");
    }
}
