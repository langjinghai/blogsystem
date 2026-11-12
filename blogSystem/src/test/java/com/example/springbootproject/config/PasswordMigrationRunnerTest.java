package com.example.springbootproject.config;

import com.example.springbootproject.mapper.UserMapper;
import com.example.springbootproject.pojo.User;
import com.example.springbootproject.utils.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordMigrationRunnerTest {

    @Test
    void migratesOnlyPlaintextPasswords() {
        UserMapper mapper = mock(UserMapper.class);
        User plain = new User();
        plain.setId(1L);
        plain.setPassword("old-password");
        User hashed = new User();
        hashed.setId(2L);
        hashed.setPassword(PasswordUtils.encode("keep-password"));

        when(mapper.selectList(null)).thenReturn(List.of(plain, hashed));

        new PasswordMigrationRunner(mapper).run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mapper, times(1)).updateById(captor.capture());
        String migrated = captor.getValue().getPassword();
        assertTrue(PasswordUtils.isBcrypt(migrated));
        assertTrue(PasswordUtils.matches("old-password", migrated));
    }
}
