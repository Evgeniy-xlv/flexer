# ABOUT

-----------

Flexer is a simple library allowing to use conditions in their spring data queries.

# Example

-----------

```java
import c0rnell.flexer.query.ConditionalQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyRepository extends JpaRepository<MyEntity, Long> {

    @ConditionalQuery
    @Query(
            value = """
                    SELECT me.* FROM my_entity me
                    
                    -- The sections below between '--%' and '--/' will be preprocessed by library
                    -- and according to the result of it's conditions defined with SPeL expression
                    -- it will be cut off or replaced by the query sequence inside the structure
                    
                    --%#{#includeMoreInfo}
                        JOIN my_entity_ext_data ed ON me.id = ed.my_entity_id
                    --/
                    WHERE me.common_condition = TRUE
                    --%#{#filterSpecifically}
                      AND me.specific_condition = TRUE
                    --/
                    """,
            nativeQuery = true
    )
    List<MyEntity> findSomethingButIWantUseConditionalQueries(boolean includeMoreInfo, boolean filterSpecifically);
}
```

# Installation

-----------

### Gradle

1. Add a dependency
    ```groovy
    dependencies {
        // ...
        compile files(new File(buildDir, "c0rnell.flexer-${version}.jar"))
    }
    ```
2. Add a VM argument
    ```java
   public static void main(String[] args) {
        FlexerAttach.attach();
        // start spring app
   }
    ```