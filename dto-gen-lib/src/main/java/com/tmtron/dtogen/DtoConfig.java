package com.tmtron.dtogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: javadoc
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface DtoConfig {
}
