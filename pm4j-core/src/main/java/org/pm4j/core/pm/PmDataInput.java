package org.pm4j.core.pm;

/**
 * Interface for data input related presentation models.
 *
 * @author olaf boede
 */
public interface PmDataInput extends PmObject {

  /**
   * Enumeration of values for control of reset method.
   */
  public enum ResetReadonlyType {
    
    /** Data input PMs in ReadOnly state are reset. */
    INCLUDING_READONLY,

    /** Data input PMs in ReadOnly state are NOT reset. */
    EXCLUDING_READONLY;
  }
  
  /**
   * Indicates the change of value. In detail a new value has been entered, but has not been saved by
   * the user yet.
   * <p>
   * The changed state usually gets cleared on execution of a {@link PmCommand}
   * that required valid values.
   *
   * @return <code>true</code> if the value of this PM or one of its composite children was changed.
   */
  boolean isPmValueChanged();

  /**
   * Marks the PM manually as being changed or being unchanged.
   * <p>
   * Setting the PM to being unchanged will be propagated recursively to all child PMs.
   *
   * @param changed The new changed state.
   */
  void setPmValueChanged(boolean changed);

  /**
   * Resets the values of editable data input PMs to their default value.
   * @deprecated The method is deprecated, because it does not reset data input PMs in ReadOnly state.
   *             Please use {@link #resetPmValues(ResetReadonlyType)} instead.
   */
  @Deprecated
  void resetPmValues();

  /**
   * Resets the values of an attribute to <code>null</code> or their optional default value definition,
   * can include or exclude attributes in ReadOnly state.
   * 
   * @param resetReadonlyType Controls whether attributes in ReadOnly state are reset.
   *        The parameter of type PmResetType can have two valid values:
   *        <ul>
   *          <li>INCLUDING_READONLY: attributes in ReadOnly state are reset.</li>
   *          <li>EXCLUDING_READONLY: attributes in ReadOnly state are NOT reset.</li>
   *        </ul>
   */
  void resetPmValues(ResetReadonlyType resetReadonlyType);

  /**
   * Returns <code>true</code> when each PM value modification will be applied just to an edit buffer.
   * Changed values will be applied to the data store behind the presentation model by
   * calling {@link #commitBufferedPmChanges()}, or discarded by calling {@link #rollbackBufferedPmChanges()}.
   * 
   * @return <code>True</code> when the data input PM is in edit buffer mode.
   */
  boolean isBufferedPmValueMode();

  /**
   * Commits all changed values from edit buffer to the data store behind the presentation model.
   */
  void commitBufferedPmChanges();

  /**
   * Clears all uncommitted changes in edit buffer.
   * <p>
   * Does not change values of the data store behind the presentation model.
   */
  void rollbackBufferedPmChanges();

  /**
   * Validates this PM.<br>
   * Generates error messages in case of validation problems.<br>
   * Fires {@link PmEvent#VALIDATION_STATE_CHANGE} events in case of a change of the valid-state.
   */
  // TODO olaf: move public interface to validation API. Change to protected implementation method.
  void pmValidate();

}
