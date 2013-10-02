package org.pm4j.core.pm.api;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmDataInput.ResetReadonlyType;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi.VisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.VisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.VisitResult;

public class PmModificationApi {

  public PmModificationApi() {
  }

  /**
   * Resets the values of an attribute to <code>null</code> or their optional default value definition,
   * can include or exclude attributes in ReadOnly state.
   * 
   * @param startPm The PM where the reset starts, and eventually continues on its children.
   * @param resetReadonlyType Controls whether attributes in ReadOnly state are reset.
   *        The parameter of type PmResetType can have two valid values:
   *        <ul>
   *          <li>INCLUDING_READONLY: attributes in ReadOnly state are reset.</li>
   *          <li>EXCLUDING_READONLY: attributes in ReadOnly state are NOT reset.</li>
   *        </ul>
   */
  public static void resetPmValues(PmObject startPm, ResetReadonlyType resetReadonlyType) {
    assert startPm != null;
    assert resetReadonlyType != null;
    switch (resetReadonlyType) {
    
      case EXCLUDING_READONLY:
        VisitCallBack callBackExcludingReadOnly = new VisitCallBack() {
          @Override
          public VisitResult visit(PmObject pm) {
            if (pm instanceof PmDataInput) {
              ((PmDataInput) pm).resetPmValues(ResetReadonlyType.EXCLUDING_READONLY);
            }
            return VisitResult.CONTINUE;
          }
        };
        PmVisitorApi.visitChildren(startPm, callBackExcludingReadOnly,
            VisitHint.SKIP_NOT_INITIALIZED, // Not yet initialized PMs are not yet reset for sure.
            VisitHint.SKIP_CONVERSATION,    // Conversations have their own reset handling.
            VisitHint.SKIP_INVISIBLE,       // Invisible parts should not be reset.
            VisitHint.SKIP_READ_ONLY);      // Read only parts should not be reset.
        break;
        
      case INCLUDING_READONLY:
        VisitCallBack callBackIncludingReadonly = new VisitCallBack() {
          @Override
          public VisitResult visit(PmObject pm) {
            if (pm instanceof PmDataInput) {
              ((PmDataInput) pm).resetPmValues(ResetReadonlyType.INCLUDING_READONLY);
            }
            return VisitResult.CONTINUE;
          }
        };
        PmVisitorApi.visitChildren(startPm, callBackIncludingReadonly,
            VisitHint.SKIP_NOT_INITIALIZED, // Not yet initialized PMs are not yet reset for sure.
            VisitHint.SKIP_CONVERSATION,    // Conversations have their own reset handling.
            VisitHint.SKIP_INVISIBLE);      // Invisible parts should not be reset.
        break;
        
      default:
        throw new PmRuntimeException("Unknown option of " 
          + ResetReadonlyType.class.getSimpleName() + "." + resetReadonlyType);
    }
  }

}
