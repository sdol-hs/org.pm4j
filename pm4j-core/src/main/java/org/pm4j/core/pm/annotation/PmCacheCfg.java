package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache definition annotation.
 * <p>
 * TODOC olaf:
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmCacheCfg {

  public enum CacheMode {
    /** Not specified. */
    NOT_SPECIFIED,
    /** No caching. */
    OFF,
    /** The value will be cached locally within the PM instance. */
    ON,
    /**
     * The value will be cached for the life time of a request.
     * <p>
     * This option will currently only be considered in the JSF environment. In
     * other cases (rich client) it has the effect of the option {@link #OFF}.
     */
    REQUEST
  }

  /**
   * @return <code>true</code> when all states should be cached.
   */
  CacheMode all() default CacheMode.NOT_SPECIFIED;

  /**
   * @return <code>true</code> when visibility states should be cached.
   */
  CacheMode visibility() default CacheMode.NOT_SPECIFIED;

  /**
   * @return <code>true</code> when enabled states should be cached.
   */
  CacheMode enablement() default CacheMode.NOT_SPECIFIED;

  /**
   * @return <code>true</code> when titles should be cached.
   */
  CacheMode title() default CacheMode.NOT_SPECIFIED;

  /**
   * @return <code>true</code> when tooltip's should be cached.
   */
// XXX olaf: not yet done because it was not in the profiler hit list.
//  CacheMode tooltip() default CacheMode.UNKNOWN;

  /**
   * @return The value caching strategy.
   */
  CacheMode value() default CacheMode.NOT_SPECIFIED;

  /**
   * @return The option set caching strategy..
   */
  CacheMode options() default CacheMode.NOT_SPECIFIED;

  /**
   * A cache definition is by default only applied for the PM that you annotate.
   * <p>
   * In some cases it's useful to define a cache mode for a whole sub tree of PM's.
   * If you define {@link #cascade()} as <code>true</code> the cache definition
   * will also be applied for all child PM's.<br>
   * But: Each recursively defined parent cache aspect definition can be overridden
   * by a cache aspect definition on child level.
   *
   * @return <code>true</code> if the cache definition should also be applied on child PMs.
   */
  boolean cascade() default false;

  // Name constants for attributes that are found by reflection:
  public static final String ATTR_VISIBILITY = "visibility";
  public static final String ATTR_ENABLEMENT = "enablement";
  public static final String ATTR_TITLE = "title";
  public static final String ATTR_VALUE = "value";
  public static final String ATTR_OPTIONS = "options";

}
