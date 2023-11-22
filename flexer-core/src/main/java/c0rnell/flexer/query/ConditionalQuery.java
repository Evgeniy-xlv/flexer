package c0rnell.flexer.query;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method contained by {@link Repository} to enable the support of conditional queries.
 * Also requires method to be annotated with {@link Query} to extract the source query to process conditions.
 *
 * @see QueryParameterExtractorAspect
 * */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalQuery {
}
