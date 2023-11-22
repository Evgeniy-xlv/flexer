package c0rnell.flexer.test;

import lombok.Builder;
import lombok.NonNull;

@Builder
public class Builable {

    @NonNull
    private Long id;

    private String who;

    private String where;
}
