package com.krokochik.ideasforum.api.annotation.aspect;

import com.krokochik.ideasforum.api.annotation.Processor;
import com.krokochik.ideasforum.api.processor.RequestProcessor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class ProcessorAspect {

    @Autowired
    AspectService aspectService;

    @Around("@annotation(annotation)")
    public Object validate(ProceedingJoinPoint joinPoint, Processor annotation) throws Throwable {
        Object[] args = joinPoint.getArgs();

        Method method = aspectService.getMethod(joinPoint);
        if (method == null) {
            throw new IllegalArgumentException("Method not found");
        }

        if (args.length == 0) {
            return joinPoint.proceed();
        }

        Class<? extends RequestProcessor> validatorClass = annotation.value();
        RequestProcessor processor = validatorClass.newInstance();
        aspectService.setFieldValues(processor,
                aspectService.getParameters(args, method));
        aspectService.injectVariables(processor,
                joinPoint.getTarget(), method);

        for (int i = args.length - 1; i >= 0; i--) {
            if (RequestProcessor.class.isAssignableFrom(
                    method.getParameters()[i].getType())) {
                args[i] = processor;
                break;
            }
        }

        return joinPoint.proceed(args);
    }

}
