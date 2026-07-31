package com.mahendhar.urlshortener.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void lifecycleCallbackNormalizesEmailAndSetsAuditFields() {
        User user = User.builder().email("MEMBER@EXAMPLE.COM").enabled(false).build();

        user.onCreate();

        assertThat(user.getEmail()).isEqualTo("member@example.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }
}
