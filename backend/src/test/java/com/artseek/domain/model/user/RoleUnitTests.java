package com.artseek.domain.model.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.exceptions.user.InvalidRoleException;
import com.artseek.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;

class RoleUnitTests {

    @Test
    void mapsEachRoleToItsStableStringValue() {
        assertThat(Role.USER.value()).isEqualTo("user");
        assertThat(Role.ADMIN.value()).isEqualTo("admin");
    }

    @Test
    void parsesARoleFromItsStringValue() {
        assertThat(Role.fromValue(" ADMIN ")).isEqualTo(Role.ADMIN);
        assertThat(Role.fromValue("user")).isEqualTo(Role.USER);
    }

    @Test
    void rejectsUnknownOrMissingRoles() {
        assertThatThrownBy(() -> Role.fromValue("anonymous"))
                .isInstanceOf(InvalidRoleException.class)
                .hasMessage(InvalidRoleException.MESSAGE)
                .extracting(exception -> ((InvalidRoleException) exception).getInvalidRole())
                .isEqualTo("anonymous");
        assertThatThrownBy(() -> Role.fromValue(null))
                .isInstanceOf(InvalidRoleException.class)
                .extracting(exception -> ((InvalidRoleException) exception).getInvalidRole())
                .isNull();
    }

    @Test
    void reportsTheRejectedFieldThroughTheSharedValidationType() {
        assertThatThrownBy(() -> Role.fromValue("x"))
                .isInstanceOf(DomainValidationException.class)
                .extracting(exception -> ((DomainValidationException) exception).getField())
                .isEqualTo(InvalidRoleException.FIELD)
                .isEqualTo("role");
    }
}
