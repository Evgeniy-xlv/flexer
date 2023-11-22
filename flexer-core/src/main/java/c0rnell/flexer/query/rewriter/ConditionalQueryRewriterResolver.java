package c0rnell.flexer.query.rewriter;

import c0rnell.flexer.query.ConditionalQuery;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This resolver is called when {@link c0rnell.flexer.asm.FlexerInstrumentationAgent} is used.
 * It resolves a conditional query rewriter version for loaded spring-boot.
 * */
public final class ConditionalQueryRewriterResolver {

    private static final ConditionalQueryRewriterResolver INSTANCE = new ConditionalQueryRewriterResolver();

    private final ConditionalQueryFakeRewriter conditionalQueryFakeRewriter = new ConditionalQueryFakeRewriter();

    private ConditionalQueryRewriter conditionalQueryRewriter;

    private ConditionalQueryRewriterResolver() {
    }

    public ConditionalQueryRewriter resolve(QueryMethod queryMethod) {
        try {
            Field methodField = QueryMethod.class.getDeclaredField("method");
            methodField.setAccessible(true);
            if (((Method) methodField.get(queryMethod)).getAnnotation(ConditionalQuery.class) != null) {
                if (conditionalQueryRewriter == null) {
                    lazyInitializeConditionalQueryRewriter();
                }
                return conditionalQueryRewriter;
            }
        } catch (Throwable t) {
            throw new RuntimeException("An error occurred during resolving conditional query rewriter", t);
        }
        return conditionalQueryFakeRewriter;
    }

    private void lazyInitializeConditionalQueryRewriter() {
        try {
            Class.forName("org.springframework.data.jpa.repository.QueryRewriter");
            conditionalQueryRewriter = (ConditionalQueryRewriter) Class.forName("c0rnell.flexer.query.rewriter.ConditionalQueryRewriterV3")
                    .getConstructor()
                    .newInstance();
        } catch (Exception e) {
            conditionalQueryRewriter = new ConditionalQueryRewriterV2();
        }
    }

    public static ConditionalQueryRewriterResolver getInstance() {
        return INSTANCE;
    }

    public static String resolveThenRewrite(QueryMethod queryMethod, String query) {
        return getInstance().resolve(queryMethod).rewrite(query);
    }
}
