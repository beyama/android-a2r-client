package eu.addicted2random.a2rclient.jsonrpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class as JSON-RPC 2 Service.
 * 
 * @author Alexander Jentz, beyama.de
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONRPCService {
  String value() default "";
}
