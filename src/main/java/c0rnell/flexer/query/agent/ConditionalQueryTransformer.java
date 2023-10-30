package c0rnell.flexer.query.agent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ReturnedType;

/**
 * A main transformer that's used to modify {@link org.springframework.data.jpa.repository.query.NativeJpaQuery#createJpaQuery(String, Sort, Pageable, ReturnedType)}.
 * It will be used to preprocess conditional query logic in cases there's no {@link org.springframework.data.jpa.repository.QueryRewriter} in the classpath (spring-boot version < 3)
 *
 * @see org.springframework.data.jpa.repository.query.NativeJpaQuery#createJpaQuery(String, Sort, Pageable, ReturnedType)
 * @see Transformer
 * */
public class ConditionalQueryTransformer extends Transformer {

    public ConditionalQueryTransformer() {
        super("org.springframework.data.jpa.repository.query.NativeJpaQuery", "createJpaQuery");
    }

    /**
     * Inserts a simple code part before the first instruction of the target method:
     * <blockquote><pre>
     * Field methodField = QueryMethod.class.getDeclaredField("method");
     * methodField.setAccessible(true);
     * queryString = ConditionalQueryRewriterResolver.getInstance()
     *         .resolve(this.getQueryMethod())
     *         .rewrite(queryString);
     * </pre></blockquote>
     *
     * @see org.springframework.data.jpa.repository.query.NativeJpaQuery#createJpaQuery(String, Sort, Pageable, ReturnedType)
     * */
    @Override
    protected void transform(ClassPool classPool, CtClass clazz, CtMethod method) throws CannotCompileException {
        method.insertBefore(
                "java.lang.reflect.Field methodField = org.springframework.data.repository.query.QueryMethod.class.getDeclaredField(\"method\");" +
                    "methodField.setAccessible(true);" +
                    "queryString = c0rnell.flexer.query.rewriter.ConditionalQueryRewriterResolver.getInstance()" +
                    "        .resolve(this.getQueryMethod())" +
                    "        .rewrite(queryString);"
        );
    }
}
