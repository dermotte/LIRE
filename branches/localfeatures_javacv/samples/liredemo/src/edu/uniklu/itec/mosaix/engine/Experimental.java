package edu.uniklu.itec.mosaix.engine;

import java.lang.annotation.*;

/**
 * Marks implementations as HIGHLY experimental and
 * possibly subject to change.
 *
 * @author Manuel Warum
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Experimental {
}
