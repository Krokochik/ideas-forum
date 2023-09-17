package com.krokochik.ideasforum.api.annotation.aspect;

import com.krokochik.ideasforum.api.annotation.FromParent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class AspectService {
    Method getMethod(ProceedingJoinPoint joinPoint) {
        Method[] methods = joinPoint.getTarget().getClass().getMethods();
        String methodName = joinPoint.getSignature().getName();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    Map<String, Object> getParameters(Object[] args, Method method) {
        Map<String, Object> parameters = new HashMap<>();
        for (int i = 0; i < args.length - 1; i++) {
            parameters.put(method.getParameters()[i].getName(), args[i]);
        }
        return parameters;
    }

    void setFieldValues(Object object, Map<String, Object> fieldValues) {
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = field.getName();
            if (fieldValues.containsKey(fieldName)) {
                System.out.println(fieldName);
                Object value = fieldValues.get(fieldName);
                try {
                    field.setAccessible(true);
                    field.set(object, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    void injectVariables(Object target, Object aspectTarget, Method method) {
        try {
            Field[] fields = target.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Arrays.stream(field.getAnnotations())
                        .anyMatch(annotation -> annotation.annotationType() == FromParent.class)) {
                    for (Field parentField : method.getDeclaringClass().getDeclaredFields()) {
                        if (parentField.getType() == field.getType()) {
                            parentField.setAccessible(true);
                            Object value = parentField.get(aspectTarget);
                            if (value != null) {
                                field.setAccessible(true);
                                field.set(target, value);
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
