package edu.epam.fop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@within(edu.epam.fop.annotation.Logging) || @annotation(edu.epam.fop.annotation.Logging) || within(edu.epam.fop.service..*)")
    public Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("{} executed in {} ms", pjp.getSignature(), duration);
            return result;
        } catch (Throwable ex) {
            log.error("Exception in {}: {}", pjp.getSignature(), ex.getMessage(), ex);
            throw ex;
        }
    }
} 