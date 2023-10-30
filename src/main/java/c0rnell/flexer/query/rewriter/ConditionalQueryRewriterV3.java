package c0rnell.flexer.query.rewriter;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.QueryRewriter;

/**
 * It's only used for spring-boot 3.x.x versions
 * */
public class ConditionalQueryRewriterV3 extends ConditionalQueryRewriter implements QueryRewriter {

    @Override
    public String rewrite(String query, Sort sort) {
        return super.rewrite(query);
    }
}
