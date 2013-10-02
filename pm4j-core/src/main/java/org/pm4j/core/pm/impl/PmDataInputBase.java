package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.VisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.VisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.VisitResult;

/**
 * Abstract base class for data input related presentation models.
 * 
 * @author olaf boede
 */
public abstract class PmDataInputBase extends PmObjectBase implements PmDataInput {

  /** <code>True</code> if this PM instance is being marked to be changed. */
  private boolean pmExpliciteChangedFlag;

  /**
   * Constructor of PmDataInputBase.
   * 
   * @param pmParent
   *          The context, this PM was created in, e.g. a session, a command, a list field.
   */
  public PmDataInputBase(PmObject parentPm) {
    super(parentPm);
  }

  /**
   * Indicates the change of value. In detail a new value has been entered, but has not been saved by
   * the user yet. This is the default framework implementation. To customize the logic please override
   * {@link #isPmValueChangedImpl()}.
   * <p>
   * The changed state usually gets cleared on execution of a {@link PmCommand}
   * that required valid values.
   *
   * @return <code>true</code> if the value of this PM or one of its composite children was changed.
   */
  @Override
  public final boolean isPmValueChanged() {
    // Extension point for changed state caching.

    // A PM not initialized yet is always unchanged.
    return (this.pmInitState == PmInitState.INITIALIZED) &&
           this.isPmValueChangedImpl();
  }

  /**
   * Override this method to customize the framework logic, under which circumstances a PM's value 
   * is stated to being changed. The default framework implementation returns <code>true</code>,
   * if the PM itself or at least one of its descendants has been changed, excluding descendants 
   * which are not initialized, invisible, readonly or of type {@link PmConversation}.
   * 
   * @return <code>true</code> if the PM itself or at least one of its descendants has been changed.
   */
  protected boolean isPmValueChangedImpl() {
    if (this.pmExpliciteChangedFlag) {
      return true;

    } else {
      // XXX olaf: the tree related question should be factored out to a utility.
      // TODO okossak Visitor pattern einführen?
      // getPmChildrenOfType() returns direct children, but not all descendants
      // if a child not of type PmDataInput has children of type PmDataInput, those will not be asked
      for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
  
        if (PmInitApi.isPmInitialized(d) && // a not initialized PM can't have a change.
            d.isPmVisible() && !d.isPmReadonly() && // invisible and readonly can't have a change.
      	    (!(d instanceof PmConversation)) && // a sub-conversation does not influence the changed state
      	    d.isPmValueChanged()) {
          return true;
        }
      }
    }
    // if no changes are found
    return false;
  }

  /**
   * Marks the PM manually as being changed or being unchanged. The 
   * <p>
   * Resetting the PM to change flag to <code>false</code> is invoked from 
   * {@link PmCommandImpl#afterDo(boolean)}, it will be propagated recursively to all its descendants,
   * excluding those which are not initialized, invisible, readonly or of type {@link PmConversation}.
   *
   * @param changed The new change state.
   */
  @Override
  public final void setPmValueChanged(final boolean changed) {
    boolean changedStateChanged = _setPmValueChangedForThisInstanceOnly(this, changed);

    if (changedStateChanged) {
      // Inform about the change directly:
      PmEventApi.firePmEvent(this, PmEvent.VALUE_CHANGE);

      // Collects the state change information to send the related event to all changed PMs,
      // but just in case the propagation to descendants has completed without abort.
      final List<PmObject> pmsOfChangedStateChange = new ArrayList<PmObject>();
      // TODO undo 
      pmsOfChangedStateChange.add(this);

      // Only if the changed flag was set to 'false', reset the changed states for all descendants.
        if ((changed == false) && (pmInitState == PmInitState.INITIALIZED)) {
        VisitCallBack callBack = new VisitCallBack() {
          @Override
          public VisitResult visit(PmObject pm) {
            if (pm instanceof PmDataInputBase) {
              if (_setPmValueChangedForThisInstanceOnly((PmDataInputBase)pm, changed)) {
                pmsOfChangedStateChange.add(pm);
              }
            } else if (pm instanceof PmDataInput) {
              // If we find a different implementation we have to call the external interface.
              // XXX olaf: the children of this child may get duplicate calls when the visitor proceeds...
              ((PmDataInput) pm).setPmValueChanged(changed);
            }
            return VisitResult.CONTINUE;
          }
        };

        // If some of the by default skipped PMs should be traversed too: Please override setPmValueChangedImpl().
        PmVisitorApi.visitChildren(this, callBack,
            VisitHint.SKIP_NOT_INITIALIZED, // Not yet initialized PMs are not yet changed for sure.
            VisitHint.SKIP_CONVERSATION,    // Conversations have their own change handling.
            VisitHint.SKIP_INVISIBLE,       // Invisible parts should not be changed.
            VisitHint.SKIP_READ_ONLY);      // Read only parts should never be changed.
      }

      // Inform about changed state changes of changed PMs.
      for (PmObject pm : pmsOfChangedStateChange) {
        PmEventApi.firePmEvent(pm, PmEvent.VALUE_CHANGED_STATE_CHANGE);
      }
    }
  }

  /**
   * Override this method to extend the framework logic for the moment the value of a PM's 
   * change state is changed. The default framework implementation of this method is empty.
   * 
   * @param changed The new explicitly assigned changed state.
   */
  protected void setPmValueChangedImpl(boolean changed) {
  }

  /**
   * Internal helper that adjusts the changed state for this PM only.
   * @param pm
   * @param newChangedState
   * @return <code>true</code> if the changed state of the PM was changed by this call.
   */
  private static boolean _setPmValueChangedForThisInstanceOnly(PmDataInputBase pm, boolean newChangedState) {
    // store change state temporarily
    boolean wasPmValueChanged = pm.isPmValueChanged();

    pm.pmExpliciteChangedFlag = newChangedState;
    pm.setPmValueChangedImpl(newChangedState);

    // return true, if the change state has been changed
    return wasPmValueChanged != newChangedState;
  }

  /**
   * @deprecated The method is deprecated, because it does not reset attributes in ReadOnly state.
   *             Please use {@link #resetPmValues(ResetReadonlyType)} instead.
   */
  @Deprecated
  @Override
  public final void resetPmValues() {
    resetPmValues(ResetReadonlyType.EXCLUDING_READONLY);
  }

  @Override
  public void resetPmValues(ResetReadonlyType readonlyType) {
    for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
      d.resetPmValues(readonlyType);
    }
  }

  /**
   * Validates this PM. Generates error messages in case of validation problems. Fires 
   * {@link PmEvent#VALIDATION_STATE_CHANGE} events in case of a change of the valid-state.
   * <p>
   * This default implementation validates the attributes. Subclasses may override this to 
   * provide some more specific logic.
   * <p>
   * Important for overriding: Don't forget to call <code>super.pmValidate()</code> 
   * to ensure attribute validation.
   */
  // TODO olaf: move to validation API.
  // TODO okossak Visitor pattern einführen?
  // getPmChildrenOfType() returns direct children, but not all descendants
  // if a child not of type PmDataInput has children of type PmDataInput, those will not be asked
  @Override
  public void pmValidate() {
    if (isPmVisible() && !isPmReadonly()) {
      for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
        if (d.isPmVisible() && !d.isPmReadonly()) {
          d.pmValidate();
        }
      }
    }
  }

  // ======== Buffered data input support ======== //

  /**
   * Returns <code>true</code> when each PM value modification will be applied just to an edit buffer.
   * Changed values will be applied to the data store behind the presentation model by
   * calling {@link #commitBufferedPmChanges()}, or discarded by calling {@link #rollbackBufferedPmChanges()}.
   * 
   * @return <code>True</code> when the data input PM is in edit buffer mode.
   */
  @Override
  public boolean isBufferedPmValueMode() {
    return getPmConversation().isBufferedPmValueMode();
  }

  /**
   * Commits all changed values from edit buffer to the data store behind the presentation model.
   */
  @Override
  public void commitBufferedPmChanges() {
    // TODO okossak Visitor pattern einführen?
    // getPmChildrenOfType() returns direct children, but not all descendants
    // if a child not of type PmDataInput has children of type PmDataInput, those will not be asked
    for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
      d.commitBufferedPmChanges();
    }
  }

  /**
   * Clears all uncommitted changes in edit buffer.
   * <p>
   * Does not change values of the data store behind the presentation model.
   */
  @Override
  public void rollbackBufferedPmChanges() {
    // TODO okossak Visitor pattern einführen?
    // getPmChildrenOfType() returns direct children, but not all descendants
    // if a child not of type PmDataInput has children of type PmDataInput, those will not be asked
    for (PmDataInput d : PmUtil.getPmChildrenOfType(this, PmDataInput.class)) {
      d.rollbackBufferedPmChanges();
    }
  }

}
