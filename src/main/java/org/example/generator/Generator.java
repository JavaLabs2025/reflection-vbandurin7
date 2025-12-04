package org.example.generator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Generator {
    private static final String GENERATABLE_CLASSES_PACKAGE = "org.example.classes";
    private static final Integer MAX_DEPTH = 3;
    private static final Integer COLLECTION_OBJECT_COUNT = 5;
    private static final Random random = new Random();
    private static final Map<Class<?>, Supplier<Object>> BASIC_TYPE_FUNCTIONS;

    static {
        Map<Class<?>, Supplier<Object>> map = new HashMap<>();
        map.put(byte.class, () -> (byte) random.nextInt(256));
        map.put(Byte.class, () -> (byte) random.nextInt(256));
        map.put(short.class, () -> (short) random.nextInt(65536));
        map.put(Short.class, () -> (short) random.nextInt(65536));
        map.put(int.class, random::nextInt);
        map.put(Integer.class, random::nextInt);
        map.put(long.class, random::nextLong);
        map.put(Long.class, random::nextLong);
        map.put(float.class, random::nextFloat);
        map.put(Float.class, random::nextFloat);
        map.put(double.class, random::nextDouble);
        map.put(Double.class, random::nextDouble);
        map.put(boolean.class, random::nextBoolean);
        map.put(Boolean.class, random::nextBoolean);
        map.put(String.class, () -> "str_" + random.nextInt());

        BASIC_TYPE_FUNCTIONS = Collections.unmodifiableMap(map);
    }

    public Object generateValueOfType(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return generateValueOfTypeInternal(clazz, 0);
    }

    private Object generateValueOfTypeInternal(Class<?> clazz, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (clazz == null) {
            throw new IllegalAccessException("Unable to generate instance for null class");
        }

        if (!clazz.isAnnotationPresent(Generatable.class)) {
            throw new IllegalAccessException("Unable to generate instance for non generatable class");
        }

        if (depth == MAX_DEPTH) {
            return null;
        }

        if (clazz.isInterface()) {
            Class<?> implementation = findImplementation(clazz);
            return generateValueOfTypeInternal(implementation, depth);
        }
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        int randomConstructorIndex = new Random().nextInt(constructors.length);
        Constructor<?> constructor = constructors[randomConstructorIndex];

        Parameter[] parameters = constructor.getParameters();
        if (parameters.length == 0) {
            return constructor.newInstance();
        }

        Object[] initializedParams = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            initializedParams[i] = generateConstructorValue(parameter, depth + 1);
        }
        return constructor.newInstance(initializedParams);
    }

    private Object generateConstructorValue(Parameter parameter, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> parameterType = parameter.getType();
        if (BASIC_TYPE_FUNCTIONS.containsKey(parameterType)) {
            return generatePrimitiveOrStringValue(parameterType);
        }
        if (Collection.class.isAssignableFrom(parameterType)) {
            return generateCollection(parameterType, parameter.getParameterizedType(), depth);
        }
        return generateValueOfTypeInternal(parameterType, depth);
    }

    private Collection<?> generateCollection(Class<?> parameterType, Type genericType, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Collection<Object> result;
        if (List.class.isAssignableFrom(parameterType)) {
            result = new ArrayList<>();
        } else {
            throw new RuntimeException("Collection type " + parameterType + "is not supported");
        }

        fillCollection(result, genericType, depth);
        return result;
    }

    private void fillCollection(Collection<Object> result, Type genericType, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!(genericType instanceof ParameterizedType)) {
            return;
        }
        ParameterizedType pType = (ParameterizedType) genericType;
        Type[] actualTypeArguments = pType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            return;
        }
        Type typeArgument = actualTypeArguments[0];
        if (typeArgument instanceof Class<?> cl) {
            for (int i = 0; i < COLLECTION_OBJECT_COUNT; i++) {
                result.add(generateValueOfTypeInternal(cl, depth + 1));
            }
        }
    }

    private Object generatePrimitiveOrStringValue(Class<?> parameterType) {
        Supplier<Object> valueGenerator = BASIC_TYPE_FUNCTIONS.get(parameterType);
        if (valueGenerator == null) {
            throw new IllegalArgumentException("Unexpected parameter type, cannot generate primitive value for " + parameterType.toString());
        }
        return valueGenerator.get();
    }

    private Class<?> findImplementation(Class<?> clazz) {
        List<Class<?>> allClassesUsingClassLoader = findAllClassesUsingClassLoader(GENERATABLE_CLASSES_PACKAGE);
        List<Class> implementations = allClassesUsingClassLoader.stream()
                .filter(cl -> cl.isAnnotationPresent(Generatable.class))
                .filter(cl -> !cl.isInterface())
                .filter(clazz::isAssignableFrom)
                .collect(Collectors.toList());
        if (implementations.isEmpty()) {
            throw new RuntimeException("Implementation not found for class " + clazz.getSimpleName());
        }
        return implementations.get(random.nextInt(implementations.size()));
    }

    public List<Class<?>> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        if (stream == null) {
            throw new RuntimeException("Unable to get classloader resources from package " + GENERATABLE_CLASSES_PACKAGE);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to find class to load: " + className);
        }
        return null;
    }
}
