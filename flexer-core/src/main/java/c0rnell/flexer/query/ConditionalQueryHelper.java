package c0rnell.flexer.query;

import c0rnell.flexer.query.exception.ConditionalQueryNotValidException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class to process conditional queries
 * */
public final class ConditionalQueryHelper {

    private static final String CONDITION_PREFIX = "--%";
    private static final String CONDITION_SUFFIX = "--/";
    private static final String EXPRESSION_PREFIX = "#{";
    private static final String EXPRESSION_SUFFIX = "}";

    private static final ConditionalQueryHelper INSTANCE = new ConditionalQueryHelper();

    private final SpelExpressionParser expressionParser = new SpelExpressionParser();

    private ConditionalQueryHelper() {
    }

    /**
     * It processes passed query with conditions and parameter values
     * */
    static String processConditions(String query,
                                    List<QueryCondition> conditions,
                                    Map<String, Object> parameterValueByItsNameMap) {
        if (conditions.isEmpty()) {
            return query;
        }
        Map<String, Boolean> processedExpressions = new HashMap<>();
        String result = query;
        for (QueryCondition condition : conditions) {
            Boolean value;
            if (processedExpressions.containsKey(condition.getExpression())) {
                value = processedExpressions.get(condition.getExpression());
            } else {
                Expression expression = INSTANCE.expressionParser.parseExpression(condition.getExpression());
                for (Map.Entry<String, Object> e : parameterValueByItsNameMap.entrySet()) {
                    ((SpelExpression) expression).getEvaluationContext().setVariable(e.getKey(), e.getValue());
                }
                value = Objects.requireNonNull(expression.getValue(Boolean.class));
                processedExpressions.put(condition.getExpression(), value);
            }
            result = result.replace(condition.getCondition(), value ? condition.getSequence() : "");
        }
        return result;
    }

    /**
     * It finds all query segments starting with '--%' prefix and ending with '--/' suffix
     * It doesn't support nested segments
     */
    static List<QueryCondition> findQueryConditions(String query) throws ConditionalQueryNotValidException {
        return INSTANCE.findConditions(query);
    }

    private List<QueryCondition> findConditions(String query) throws ConditionalQueryNotValidException {
        List<QueryCondition> result = new ArrayList<>();

        char[] chars = query.toCharArray();
        int i = 0;
        while (i < chars.length) {
            int conditionPrefixEndIndex = findSequenceEndIndex(chars, CONDITION_PREFIX, i);
            if (conditionPrefixEndIndex > -1) {
                int expressionStartIndex = findSequenceEndIndex(chars, EXPRESSION_PREFIX, conditionPrefixEndIndex);
                if (expressionStartIndex == -1) {
                    throw new ConditionalQueryNotValidException(String.format(
                            "SPeL expression expected, but not found in query: %s",
                            query.substring(conditionPrefixEndIndex)
                    ));
                }
                int expressionEndIndex = findSequenceEndIndex(chars, EXPRESSION_SUFFIX, expressionStartIndex);
                if (expressionEndIndex == -1) {
                    throw new ConditionalQueryNotValidException(String.format(
                            "SPeL expression found, but has no ending in query: %s",
                            query.substring(expressionStartIndex)
                    ));
                }
                int conditionSuffixEndIndex = findSequenceEndIndex(chars, CONDITION_SUFFIX, expressionEndIndex);
                if (conditionSuffixEndIndex > -1) {
                    result.add(new QueryCondition(
                            createString(chars, conditionPrefixEndIndex - CONDITION_PREFIX.length() + 1, conditionSuffixEndIndex + 1),
                            createString(chars, expressionStartIndex - 1 + EXPRESSION_PREFIX.length(), expressionEndIndex + 1 - EXPRESSION_SUFFIX.length()),
                            createString(chars, expressionEndIndex + 1, conditionSuffixEndIndex + 1 - CONDITION_SUFFIX.length())
                    ));
                    i = conditionSuffixEndIndex;
                } else {
                    throw new ConditionalQueryNotValidException(String.format(
                            "Conditional query found, but has no ending in query: %s",
                            query.substring(i)
                    ));
                }
            } else {
                break;
            }
        }

        return result;
    }

    private String createString(char[] chars, int start, int end) {
        return new String(chars, start, end - start);
    }

    private int findSequenceEndIndex(char[] source, String sequence, int startIndex) {
        int sequenceIndex = 0;
        for (int i = startIndex; i < source.length; i++) {
            if (source[i] == sequence.charAt(sequenceIndex)) {
                if (++sequenceIndex == sequence.length()) {
                    return i;
                }
            } else if (sequenceIndex > 0) {
                sequenceIndex = 0;
            }
        }
        return -1;
    }
}
