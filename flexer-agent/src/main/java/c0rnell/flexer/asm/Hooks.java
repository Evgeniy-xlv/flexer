package c0rnell.flexer.asm;

import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.Collections;

final class Hooks {

    static final Hook CREATE_JPA_QUERY_HOOK = new Hook(
            "org/springframework/data/jpa/repository/query/NativeJpaQuery",
            "createJpaQuery",
            "(Ljava/lang/String;Lorg/springframework/data/repository/query/ReturnedType;)Ljavax/persistence/Query;",
            methodVisitor -> {
                            /*
                            It injects a simple code at the beginning of the method
                            NativeJpaQuery#createJpaQuery(String queryString, Sort sort, Pageable pageable, ReturnedType returnedType)

                            # queryString here is a param providing the source query from the @Query annotation
                            # this.getQueryMethod() provides the repository method that is used to detect if there is a @ConditionalQuery annotation

                            queryString = ConditionalQueryRewriterResolver.resolveThenRewrite(this.getQueryMethod(), queryString);

                            So, this simple line helps to rewrite a query before it is passed to EntityManager
                            */
                methodVisitor.visitCode();
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                methodVisitor.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "org/springframework/data/jpa/repository/query/NativeJpaQuery",
                        "getQueryMethod",
                        "()Lorg/springframework/data/jpa/repository/query/JpaQueryMethod;",
                        false
                );
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                methodVisitor.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "c0rnell/flexer/query/rewriter/ConditionalQueryRewriterResolver",
                        "resolveThenRewrite",
                        "(Lorg/springframework/data/repository/query/QueryMethod;Ljava/lang/String;)Ljava/lang/String;",
                        false
                );
                methodVisitor.visitVarInsn(Opcodes.ASTORE, 1);
                methodVisitor.visitEnd();
            }
    );

    static final Collection<Hook> HOOKS = Collections.singletonList(
            CREATE_JPA_QUERY_HOOK
    );
}
