package net.phoenix.http.reflection;

import net.phoenix.http.container.HttpOpCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks a method as a route.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    /**
     * The path of the route. This will be concatenated with the path of the class. <br> <br>
     * Examples: <br>
     * <br>
     * <code>If the class path is "/", and the method path is "/test", the full path will be "/test".</code>
     * <br> <br>
     * <code>If the class path is "/test", and the method path is "/test2", the full path will be "/test/test2".</code>
     * <br> <br>
     * Warning: <code>If the class path is "/", and the method path is "/test", the full path will be "//test", not "/test".</code>
     *
     * @return The path of the route
     */
    String path() default "";

    /**
     * The HTTP method of the route.
     *
     * @return The HTTP method of the route
     */
    HttpOpCode opCode() default HttpOpCode.GET;
}
