package c0rnell.flexer.query.rewriter;

import c0rnell.flexer.query.ConditionalQueryManager;

/**
 * A basic conditional query rewriter.
 * */
public abstract class ConditionalQueryRewriter {

    public String rewrite(String query) {
        return ConditionalQueryManager.getInstance().processConditionalQuery(query);
    }
}
