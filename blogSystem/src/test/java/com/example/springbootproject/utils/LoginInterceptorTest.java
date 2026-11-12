package com.example.springbootproject.utils;

import com.example.springbootproject.pojo.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginInterceptorTest {

    private StringRedisTemplate redis;

    @AfterEach
    void clearThreadLocal() {
        UserHolder.removeUser();
    }

    @SuppressWarnings("unchecked")
    private LoginInterceptor interceptorWithVersion(String version) {
        redis = mock(StringRedisTemplate.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);

        Map<Object, Object> userMap = new HashMap<>();
        userMap.put("id", "1");
        userMap.put("phone", "13800138000");
        userMap.put("nickName", "tester");
        userMap.put("tokenVersion", "0");

        when(redis.opsForHash()).thenReturn(hashOps);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(hashOps.entries("login:token:token")).thenReturn(userMap);
        when(valueOps.get("login:token:version:1")).thenReturn(version);

        return new LoginInterceptor(redis);
    }

    @Test
    void acceptsTokenWithCurrentVersion() throws Exception {
        LoginInterceptor interceptor = interceptorWithVersion("0");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        UserDTO user = UserHolder.getUser();
        assertNotNull(user);
        assertEquals(1L, user.getId());
    }

    @Test
    void rejectsTokenAfterPasswordChange() throws Exception {
        LoginInterceptor interceptor = interceptorWithVersion("1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        verify(redis).delete("login:token:token");
    }
}
