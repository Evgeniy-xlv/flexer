package c0rnell.flexer.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenerateModel {

    String className() default "Model";

    String methodName() default "toModel";

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Ignore {}
}
