package net.phoenix.http.reflection;

import net.phoenix.Server;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Router {

    private final static Map<String, Method> routes = new HashMap<>();

    /**
     * Generates routes for the server.
     * @throws IOException If an I/O error occurs
     * @throws URISyntaxException If a URI syntax error occurs
     * @throws ClassNotFoundException If the class is not found
     */
    public static void generateRoutes() throws IOException, URISyntaxException, ClassNotFoundException {
        List<Class> classes;
        try {
            classes = getClasses(Server.clazz.getPackage().getName());
        } catch (NullPointerException e) {
            classes = scanForAnnotation(WebHandler.class);
        }

        for (Class clazz : classes) {
            WebHandler webhandler = (WebHandler) clazz.getAnnotation(WebHandler.class);
            for(Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Route.class)) {
                    Route route = method.getAnnotation(Route.class);
                    addRoute(route.opCode().toString(), webhandler.path().concat(route.path()), method);
                }
            }
        }
    }

    /**
     * Gets the routes that have been generated.
     *
     * @return The routes that have been generated
     */
    public static Map<String, Method> getRoutes() {
        return routes;
    }

    /**
     * Routes an incoming request to the correct method.
     *
     * @param opCode The HTTP method of the request
     * @param path   The path of the request
     * @return The method to run
     */
    public static Method route(String opCode, String path) {
        return routes.get(opCode.concat(" ").concat(path));
    }

    /**
     * Adds a route to the server.
     *
     * @param opCode The HTTP method of the route
     * @param route  The path of the route
     * @param runner The method to run when a request is received
     */
    private static void addRoute(final String opCode, final String route, final Method runner) {
        routes.put(opCode.concat(" ").concat(route), runner);
    }

    /**
     * Gets all classes in the specified package.
     * @param packageName The package to search
     * @return A list of classes in the package
     * @throws IOException If an I/O error occurs
     * @throws URISyntaxException If a URI syntax error occurs
     * @throws ClassNotFoundException If the class is not found
     */
    private static List<Class> getClasses(String packageName) throws IOException, URISyntaxException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            URI uri = new URI(resource.toString());
            dirs.add(new File(uri.getPath()));
        }
        List<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.stream().filter(clazz -> clazz.isAnnotationPresent(WebHandler.class)).collect(Collectors.toList());
    }

    /**
     * Finds classes in the specified directory.
     * @param directory The directory to search
     * @param packageName The package name
     * @return A list of classes in the directory
     * @throws ClassNotFoundException If the class is not found
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    /**
     * Scans for classes with the specified annotation. This method is a fallback if the standard scan fails due to the class not being in a package
     *
     * @param annotationClass The annotation to scan for
     * @return A list of classes with the specified annotation
     */
    public static List<Class> scanForAnnotation(Class<? extends Annotation> annotationClass) {
        List<Class> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?>[] allClasses = getAllClasses(classLoader);
            for (Class<?> clazz : allClasses) {
                if (clazz.isAnnotationPresent(annotationClass)) {
                    classes.add(clazz);
                }
            }
        } catch (Exception e) {
            Server.logger.logError("Failed to scan for classes due to: " + e.getMessage());
        }

        return classes;
    }

    /**
     * Gets all classes in the classpath.
     *
     * @param classLoader The class loader to use
     * @return An array of all classes in the classpath
     * @throws IOException            If an I/O error occurs
     * @throws ClassNotFoundException If the class is not found
     */
    private static Class<?>[] getAllClasses(ClassLoader classLoader) throws IOException, ClassNotFoundException {
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

    /**
     * A class that helps find classes in the classpath.
     */
    private static class ClassFinder extends ClassLoader {
        private final String classpathEntry;

        /**
         * Creates a new ClassFinder.
         *
         * @param classpathEntry The classpath entry to search
         * @param parent         The parent class loader
         */
        ClassFinder(String classpathEntry, ClassLoader parent) {
            super(parent);
            this.classpathEntry = classpathEntry;
        }

        /**
         * Gets all classes in the classpath.
         *
         * @return An array of all classes in the classpath
         * @throws IOException            If an I/O error occurs
         * @throws ClassNotFoundException If the class is not found
         */
        Class<?>[] getClasses() throws IOException, ClassNotFoundException {
            int count = 0;
            Class<?>[] classes = new Class<?>[1000];
            URL url = new java.io.File(classpathEntry).toURI().toURL();
            try (URLClassLoader cl = new URLClassLoader(new URL[]{url}, this.getParent())) {
                File dir = new java.io.File(classpathEntry);
                if (dir.exists() && dir.isDirectory()) {
                    for (java.io.File file : Objects.requireNonNull(dir.listFiles())) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = file.getName().substring(0, file.getName().length() - 6);
                            Class<?> clazz = cl.loadClass(className);
                            classes[count++] = clazz;
                        }
                    }
                }
            }
            Class<?>[] result = new Class<?>[count];
            System.arraycopy(classes, 0, result, 0, count);
            return result;
        }
    }
}
