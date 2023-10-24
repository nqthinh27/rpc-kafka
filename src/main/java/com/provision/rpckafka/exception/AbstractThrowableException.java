package com.provision.rpckafka.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;

public class AbstractThrowableException extends AbstractThrowableProblem {
    public AbstractThrowableException() {
    }

    public AbstractThrowableException(@Nullable URI type) {
        super(type);
    }

    public AbstractThrowableException(@Nullable URI type, @Nullable String title) {
        super(type, title);
    }

    public AbstractThrowableException(@Nullable URI type, @Nullable String title, @Nullable StatusType status) {
        super(type, title, status);
    }

    public AbstractThrowableException(@Nullable URI type, @Nullable String title, @Nullable StatusType status, @Nullable String detail) {
        super(type, title, status, detail);
    }

    public AbstractThrowableException(@Nullable URI type, @Nullable String title, @Nullable StatusType status, @Nullable String detail, @Nullable URI instance) {
        super(type, title, status, detail, instance);
    }

    public AbstractThrowableException(@Nullable URI type, @Nullable String title, @Nullable StatusType status, @Nullable String detail, @Nullable URI instance, @Nullable ThrowableProblem cause) {
        super(type, title, status, detail, instance, cause);
    }

    public AbstractThrowableException(@Nullable URI type, @Nullable String title, @Nullable StatusType status, @Nullable String detail, @Nullable URI instance, @Nullable ThrowableProblem cause, @Nullable Map<String, Object> parameters) {
        super(type, title, status, detail, instance, cause, parameters);
    }
}
