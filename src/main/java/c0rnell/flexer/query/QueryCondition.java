package c0rnell.flexer.query;

/**
 * It's just a container for conditional query segments
 * */
public class QueryCondition {

    private final String condition;
    private final String expression;
    private final String sequence;

    protected QueryCondition(String condition, String expression, String sequence) {
        this.condition = condition;
        this.expression = expression;
        this.sequence = sequence;
    }

    public String getCondition() {
        return condition;
    }

    public String getExpression() {
        return expression;
    }

    public String getSequence() {
        return sequence;
    }
}
