package c0rnell.flexer.query;

import c0rnell.flexer.query.exception.ConditionalQueryNotValidException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It manages all conditional queries and helps them keep being faster by caching conditions
 * after they're extracted.
 * It supports sync and async calls. So you are free to use it.
 * */
public final class ConditionalQueryManager {

    private static final ConditionalQueryManager INSTANCE = new ConditionalQueryManager();

    private final ThreadLocal<Map<String, Object>> parameterValueByItsName = new ThreadLocal<>();
    private final ThreadLocal<String> methodSignature = new ThreadLocal<>();
    private final Map<String, List<QueryCondition>> cachedQueryConditionsByMethodSignature = new ConcurrentHashMap<>();

    private ConditionalQueryManager() {
    }

    /**
     * It finds conditions in the query and then caches them.
     * Then it uses short-term cached method arguments to process conditions.
     * And finally
     * @return processed, pure, beautiful and working query
     * */
    public String processConditionalQuery(String query) {
        var conditions = cachedQueryConditionsByMethodSignature.computeIfAbsent(
                getMethodSignature(),
                s -> {
                    try {
                        return ConditionalQueryHelper.findQueryConditions(query);
                    } catch (ConditionalQueryNotValidException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return processConditionalQuery(query, conditions, getParameterValueByItsNameMap());
    }

    /**
     * It finds conditions in the query and then caches them.
     * Then it uses passed method parameter values and names to process conditions.
     * And finally
     * @return processed, pure, beautiful and working query
     * */
    public String processConditionalQuery(String query, Map<String, Object> parameterValueByItsNameMap) throws ConditionalQueryNotValidException {
        var conditions = ConditionalQueryHelper.findQueryConditions(query);
        return processConditionalQuery(query, conditions, parameterValueByItsNameMap);
    }

    /**
     * It uses conditions and passed method parameter values with names to process conditions.
     * And finally
     * @return processed, pure, beautiful and working query
     * */
    public String processConditionalQuery(String query, List<QueryCondition> conditions, Map<String, Object> parameterValueByItsNameMap) {
        return ConditionalQueryHelper.processConditions(query, conditions, parameterValueByItsNameMap);
    }

    /**
     * It short-term caches method parameter values and names
     * */
    public void cacheParameters(String methodSignature, String[] parameterNames, Object[] parameterValues) {
        var params = new HashMap<String, Object>(parameterNames.length);
        for (int i = 0; i < parameterNames.length; i++) {
            params.put(parameterNames[i], parameterValues[i]);
        }
        parameterValueByItsName.set(params);
        this.methodSignature.set(methodSignature);
    }

    /**
     * It evicts cached method parameter values and names
     * */
    public void evictCacheParameters() {
        parameterValueByItsName.remove();
        methodSignature.remove();
    }

    public Map<String, Object> getParameterValueByItsNameMap() {
        return parameterValueByItsName.get();
    }

    public String getMethodSignature() {
        return methodSignature.get();
    }

    public static ConditionalQueryManager getInstance() {
        return INSTANCE;
    }
}
