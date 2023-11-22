package org.springframework.data.jpa.repository;

import org.springframework.data.domain.Sort;

/**
 * It's just a fake spring's class needed to compile the library.
 * It will be excluded from the sources after the compilation
 * */
public interface QueryRewriter {

    String rewrite(String query, Sort sort);
}
