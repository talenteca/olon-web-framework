package olon.http;

import java.lang.annotation.*;

public abstract class SnippetPFJ {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Path {
      String[] value();
    }
}

