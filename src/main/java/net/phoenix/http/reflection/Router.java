package net.phoenix.http.reflection;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {

    private final static Map<String, Method> routes = new HashMap<>();

    public static void generateRoutes() throws IOException, URISyntaxException, ClassNotFoundException {
        List<Class<?>> scannedClasses = scanForAnnotation(Webhandler.class);
        for (Class<?> clazz : scannedClasses) {
            Webhandler annotation = clazz.getAnnotation(Webhandler.class);
            String path = annotation.path();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Route routeAnnotation = method.getAnnotation(Route.class);
                if (routeAnnotation != null) {
                    String routePath = path + routeAnnotation.path();
                    addRoute(routeAnnotation.opCode().toString(), routePath, method);
                }
            }
        }
    }

    public static Map<String, Method> getRoutes() {
        return routes;
    }
    public static Method route(String opCode, String path) {
        return routes.get(opCode.concat(path));
    }

    private static void addRoute(final String opCode, final String route, final Method runner) {
        routes.put(opCode.concat(route), runner);
    }

    public static List<Class<?>> scanForAnnotation(Class<? extends Annotation> annotationClass) {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?>[] allClasses = getAllClasses(classLoader);
            for (Class<?> clazz : allClasses) {
                if (clazz.isAnnotationPresent(annotationClass)) {
                    classes.add(clazz);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    private static Class<?>[] getAllClasses(ClassLoader classLoader) throws Exception {
        String[] classpathEntries = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        int count = 0;
        Class<?>[] classes = new Class<?>[1000];
        for (String classpathEntry : classpathEntries) {
            ClassFinder finder = new ClassFinder(classpathEntry, classLoader);
            Class<?>[] foundClasses = finder.getClasses();
            for (Class<?> clazz : foundClasses) {
                classes[count++] = clazz;
            }
        }
        Class<?>[] result = new Class<?>[count];
        System.arraycopy(classes, 0, result, 0, count);
        return result;
    }

    private static class ClassFinder extends ClassLoader {
        private String classpathEntry;

        ClassFinder(String classpathEntry, ClassLoader parent) {
            super(parent);
            this.classpathEntry = classpathEntry;
        }

        Class<?>[] getClasses() throws Exception {
            int count = 0;
            Class<?>[] classes = new Class<?>[1000];
            URL url = new java.io.File(classpathEntry).toURI().toURL();
            URLClassLoader cl = new URLClassLoader(new java.net.URL[]{url}, this.getParent());
            File dir = new java.io.File(classpathEntry);
            if (dir.exists() && dir.isDirectory()) {
                for (java.io.File file : dir.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        String className = file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = cl.loadClass(className);
                        classes[count++] = clazz;
                    }
                }
            }
            Class<?>[] result = new Class<?>[count];
            System.arraycopy(classes, 0, result, 0, count);
            return result;
        }
    }


}
