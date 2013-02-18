package eu.addicted2random.a2rclient.grid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as option setter or getter.
 * @author Alexander Jentz, beyama.de
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
  public String value() default "";
}
