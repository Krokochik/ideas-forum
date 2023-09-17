package com.krokochik.ideasforum.api.annotation.aspect;

import com.krokochik.ideasforum.api.annotation.Validator;
import com.krokochik.ideasforum.api.validator.RequestValidator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

@Aspect
@Component
public class ValidatorAspect {

    @Autowired
    AspectService aspectService;

    @Around("@annotation(annotation)")
    public Object validate(ProceedingJoinPoint joinPoint, Validator annotation) throws Throwable {
        Object[] args = joinPoint.getArgs();

        Method method = aspectService.getMethod(joinPoint);
        if (method == null) {
            throw new IllegalArgumentException("Method not found");
        }

        Class<? extends RequestValidator> validatorClass = annotation.value();
        RequestValidator validator = validatorClass.newInstance();
        aspectService.setFieldValues(validator,
                 aspectService.getParameters(args, method));
        aspectService.injectVariables(validator,
                joinPoint.getTarget(), method);

        AtomicBoolean validationResult = new AtomicBoolean();
        Object validationData = validator.validate(validationResult);
        if (validationResult.get()) {
            return joinPoint.proceed();
        } else {
            return validationData;
        }
    }
}
