package io.github.tlbueno.e2e_tests.framework.common.helper;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public final class TimeHelper {

    private TimeHelper() {
        super();
    }

    public static boolean timeout(ThrowablePredicate<Boolean> predicate, long delayInMs, long timeoutInMs) {
        long realTimeout = System.currentTimeMillis() + timeoutInMs;
        while (System.currentTimeMillis() < realTimeout) {
            try {
                if (predicate.test(true)) {
                    return true;
                }
            } catch (Exception e) {
                String errMsg = "error on timing out: " + e.getMessage();
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }
        try {
            TimeUnit.MILLISECONDS.sleep(delayInMs);
        } catch (InterruptedException e) {
            String errMsg = "error on sleeping: " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return false;
    }

    @FunctionalInterface
    public interface ThrowablePredicate<T> {
        boolean test(T t) throws Exception;
    }

}
