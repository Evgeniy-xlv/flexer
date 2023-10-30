package c0rnell.flexer.query.rewriter;

/**
 * It's used to ignore query rewriting by passing the same query as the input one.
 * */
public class ConditionalQueryFakeRewriter extends ConditionalQueryRewriter {

    @Override
    public String rewrite(String query) {
        return query;
    }
}
