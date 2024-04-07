package net.phoenix.http.reflection;

import net.phoenix.http.container.HttpOpCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    String path() default "";
    HttpOpCode opCode() default HttpOpCode.GET;
}
