package com.example.ratelimiter.util;

import java.util.UUID;

public class KeyHelper {
    public static String build(UUID tenant, UUID rule, String keyValue) {
        return "ratelimit:" + tenant + ":" + rule + ":" + keyValue;
    }
}
