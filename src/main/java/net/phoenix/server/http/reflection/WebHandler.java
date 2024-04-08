package net.phoenix.server.http.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks a class as a web handler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebHandler {
    /**
     * The base path of the web handler.
     *
     * @return The path of the web handler
     */
    String path() default "/";
}
