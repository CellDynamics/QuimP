package com.github.celldynamics.quimp.plugin;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation denotes that annotated field will be escaped by specified symbols.
 * 
 * <p>Strings between escaping characters are not processed against spaces. Should be applied for
 * String type only.
 * 
 * @author p.baniukiewicz
 *
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface EscapedPath {

  /**
   * Left escaping character.
   * 
   * @return Left escaping character
   * @deprecated Not implemented in removeSpacesMacro
   */
  char left() default '[';

  /**
   * Right escaping character.
   * 
   * @return Right escaping character
   * @deprecated Not implemented in removeSpacesMacro
   */
  char right() default ']';

}
