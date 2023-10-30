package c0rnell.flexer.query;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * It helps to inject conditional query processes before and after
 * method calling of spring's repository annotated with {@link ConditionalQuery}
 * */
@Aspect
@Component
public class QueryParameterExtractorAspect {

    private MethodInvokator<Signature, String[]> getParameterNamesInvokator;

    @Around("@annotation(c0rnell.flexer.query.ConditionalQuery)")
    public Object aroundConditionalQueryAnnotation(ProceedingJoinPoint point) throws Throwable {
        var args = point.getArgs();
        var method = getSignatureMethod(point);
        if (!method.isAnnotationPresent(Query.class)) {
            throw new RuntimeException("Method annotated with ConditionalQuery must be also annotated with Query");
        }
        var parameterNames = getParameterNames(point);
        ConditionalQueryManager.getInstance().cacheParameters(point.getSignature().toString(), parameterNames, args);
        var result = point.proceed();
        ConditionalQueryManager.getInstance().evictCacheParameters();
        return result;
    }

    private Method getSignatureMethod(ProceedingJoinPoint point) {
        if (!(point.getSignature() instanceof MethodSignature)) {
            throw new RuntimeException("Cannot cast signature to MethodSignature");
        }
        return ((MethodSignature) point.getSignature()).getMethod();
    }

    private String[] getParameterNames(ProceedingJoinPoint point) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (getParameterNamesInvokator == null) {
            var classes = Class.forName("org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint", false, point.getClass().getClassLoader())
                    .getDeclaredClasses();
            for (var c : classes) {
                try {
                    getParameterNamesInvokator = new MethodInvokator<>(c.getMethod("getParameterNames"), String[].class);
                    break;
                } catch (NoSuchMethodException ignored) {
                }
            }
            if (getParameterNamesInvokator == null) {
                throw new NoSuchMethodException("getParameterNames method not found!");
            }
            ReflectionUtils.makeAccessible(getParameterNamesInvokator.method);
        }
        return getParameterNamesInvokator.invoke(point.getSignature());
    }

    private static class MethodInvokator<TARGET, RETURN_TYPE> {

        private final Method method;
        private final Class<RETURN_TYPE> returnTypeClass;

        private MethodInvokator(Method method, Class<RETURN_TYPE> returnTypeClass) {
            this.method = method;
            this.returnTypeClass = returnTypeClass;
        }

        RETURN_TYPE invoke(TARGET target) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return returnTypeClass.cast(method.invoke(target));
        }
    }
}
