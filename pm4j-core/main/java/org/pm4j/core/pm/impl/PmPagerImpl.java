package org.pm4j.core.pm.impl;

import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.DO_NOTHING;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmLabel;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.pageable.PageableCollection;
import org.pm4j.core.pm.pageable.PageableCollectionUtil;
import org.pm4j.core.pm.pageable.PageableListImpl;

/**
 * Implementation for some standard pager functionality.
 * <p>
 *
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 */
@PmTitleCfg(resKeyBase = "pmPager")
@PmBeanCfg(beanClass=PageableCollection.class)
public class PmPagerImpl<T_ITEM>
          extends PmBeanBase<PageableCollection<T_ITEM>>
          implements PmPager {

  /**
   * The set of standard pager visibility conditions.
   */
  public enum PagerVisibility {
    /** The pager will always be displayed. */
    ALWAYS,
    /** The pager will be displayed only if there is at least a second page to navigate to. */
    WHEN_SECOND_PAGE_EXISTS,
    /** The pager will be displayed only if the table has at least a single row. */
    WHEN_TABLE_IS_NOT_EMPTY,
    /** The pager will not be displayed. */
    NEVER
  }

  /** The pager visibility condition. */
  private PagerVisibility pagerVisibility = PagerVisibility.ALWAYS;

  /**
   * The changed state of this element does usually not indicate a real data
   * change Thus it is by default configured to NOT report its changes in
   * {@link #isPmValueChanged()}.<br>
   * However, this definition may be changed by setting
   * {@link #propagateChangedStateToParent} to <code>true</code>.
   */
  private boolean propagateChangedStateToParent = false;

  private PmCommandDecoratorSetImpl pageChangeDecorators = new PmCommandDecoratorSetImpl();

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdFirst = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
        return PageableCollectionUtil.hasPrevPage(getPmBean());
    }

    @Override
    protected void doItImpl() {
        PageableCollectionUtil.navigateToFirstPage(getPmBean());
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdPrev = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
        return PageableCollectionUtil.hasPrevPage(getPmBean());
    }

    @Override
    protected void doItImpl() {
        PageableCollectionUtil.navigateToPrevPage(getPmBean());
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdNext = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
      return PageableCollectionUtil.hasNextPage(getPmBean());
    }

    @Override
    protected void doItImpl() {
      PageableCollectionUtil.navigateToNextPage(getPmBean());
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdLast = new PmCommandImpl(this) {
      @Override
      protected boolean isPmEnabledImpl() {
          return PageableCollectionUtil.hasNextPage(getPmBean());
      }

      @Override
      protected void doItImpl() {
          PageableCollectionUtil.navigateToLastPage(getPmBean());
      }
  };

  public final PmLabel itemXtillYofZ = new PmLabelImpl(this) {
    @Override
    protected String getPmTitleImpl() {
        PageableCollection<T_ITEM> ps = getPmBean();
        return PmLocalizeApi.localize(this, getPmResKey(),
                PageableCollectionUtil.getIdxOfFirstItemOnPage(ps),
                PageableCollectionUtil.getIdxOfLastItemOnPage(ps),
                ps.getNumOfItems());
    }
  };

  public final PmAttrInteger currentPageIdx = new PmAttrIntegerImpl(this) {
      @Override
      protected boolean isPmEnabledImpl() {
          return getNumOfPages() > 1;
      }

      /**
       * Simply ignores invalid values.
       * <p>
       * TODO olaf: does not handle converter problems. (non numeric values)
       */
      @Override
      protected boolean setValueImpl(SetValueContainer<Integer> value) {
          Integer newValue = value.getPmValue();
          return (newValue != null) &&
                 (newValue > 0) &&
                 (newValue <= getNumOfPages());
      }


      /** Is not required. Even if the bound value is an 'int' scalar. */
      @Override
      public boolean isRequired() {
        return false;
      }

      /**
       * Refuses values out of range and considers the restrictions of registered page change decorators.
       */
      @Override
      protected void onPmInit() {
        addValueChangeDecorator(new PmAttrValueChangeDecoratorImpl<Integer>() {
          protected boolean beforeChange(PmAttr<Integer> pmAttr, Integer oldValue, Integer newValue) {
            return (newValue == null) ||
                   (newValue < 1) ||
                   (newValue >= getNumOfPages());
          }
        });
        addValueChangeDecorator(pageChangeDecorators);
      }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdSelectAllOnPage = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PageableCollectionUtil.setAllOnPageSelected(getPmBean(), Boolean.TRUE);
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdDeSelectAllOnPage = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PageableCollectionUtil.setAllOnPageSelected(getPmBean(), Boolean.FALSE);
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdSelectAll = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PageableCollection<T_ITEM> coll = getPmBean();
      for (T_ITEM i : coll.getItemsOnPage()) {
          coll.select(i);
      }
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdDeSelectAll = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PageableCollection<T_ITEM> coll = getPmBean();
      for (T_ITEM i : coll.getItemsOnPage()) {
          coll.select(i);
      }
    }
  };

  public PmPagerImpl(PmObject parent) {
    super(parent, null);
    cmdFirst.addCommandDecorator(pageChangeDecorators);
    cmdPrev.addCommandDecorator(pageChangeDecorators);
    cmdNext.addCommandDecorator(pageChangeDecorators);
    cmdLast.addCommandDecorator(pageChangeDecorators);
  }

  @Override
  protected boolean isPmVisibleImpl() {
    switch (pagerVisibility) {
      case ALWAYS:                  return true;
      case WHEN_SECOND_PAGE_EXISTS: return getPmBean().getNumOfItems() > getPageSize();
      case WHEN_TABLE_IS_NOT_EMPTY: return getPmBean().getNumOfItems() > 0;
      case NEVER:                   return false;
      default: throw new PmRuntimeException(this, "Unknown enum value: " + pagerVisibility);
    }
  }

  @Override
  public int getPageSize() {
    return getPmBean().getPageSize();
  }

  @Override
  public int getNumOfItems() {
      return getPmBean().getNumOfItems();
  }

  @Override
  public int getNumOfPages() {
      return PageableCollectionUtil.getNumOfPages(getPmBean());
  }

  /** Provides an initial empty backing bean if there is none. */
  @Override
  protected PageableCollection<T_ITEM> getPmBeanImpl() {
      return new PageableListImpl<T_ITEM>(null);
  }

  /**
   * The changed state of this element does usually not indicate a real data
   * change Thus it is by default configured to NOT report its changes in
   * {@link #isPmValueChanged()}.<br>
   * However, this definition may be changed by setting
   * {@link #propagateChangedStateToParent} to <code>true</code>.
   */
  @Override
  public boolean isPmValueChanged() {
    return  propagateChangedStateToParent &&
            super.isPmValueChanged();
  }

  @Override
  public void addPageChangeDecorator(PmCommandDecorator decorator) {
    pageChangeDecorators.addDecorator(decorator);
  }

  /**
   * PM base class for items that support item selection functionality.
   *
   * @param <T_BEAN>
   *            Type of the items.
   */
  public static class SelectableItemPm<T_BEAN> extends PmBeanBase<T_BEAN> {

      public final PmAttrBoolean selected = new PmAttrBooleanImpl(this) {
          @Override
          protected Boolean getBackingValueImpl() {
              return getPageableObjectSet().isSelected(getPmBean());
          }

          @Override
          protected void setBackingValueImpl(Boolean value) {
              if (value == Boolean.TRUE) {
                  getPageableObjectSet().select(getPmBean());
              } else {
                  getPageableObjectSet().deSelect(getPmBean());
              }
          }

          private PageableCollection<T_BEAN> getPageableObjectSet() {
              @SuppressWarnings("unchecked")
              PmPagerImpl<T_BEAN> parent = PmUtil.getPmParentOfType(this, PmPagerImpl.class);
              return parent.getPmBean();
          }
      };

      public PmAttrBoolean getSelected() {
          return selected;
      }
  }

  // -- getter / setter --

  @Override
  public PmCommand getCmdFirstPage() { return cmdFirst; }
  @Override
  public PmCommand getCmdPrevPage() { return cmdPrev; }
  @Override
  public PmCommand getCmdNextPage() { return cmdNext; }
  @Override
  public PmCommand getCmdLastPage() { return cmdLast; }
  @Override
  public PmLabel getItemXtillYofZ() { return itemXtillYofZ; }
  @Override
  public PmAttrInteger getCurrentPageIdx() { return currentPageIdx; }
  @Override
  public PmCommand getCmdSelectAllOnPage() { return cmdSelectAllOnPage; }
  @Override
  public PmCommand getCmdDeSelectAllOnPage() { return cmdDeSelectAllOnPage; }
  @Override
  public PmCommand getCmdSelectAll() { return cmdSelectAll; }
  @Override
  public PmCommand getCmdDeSelectAll() { return cmdDeSelectAll; }
  @Override
  public PagerVisibility getPagerVisibility() { return pagerVisibility; }
  /** @param pagerVisibility The pager visibility rule to use. */
  public void setPagerVisibility(PagerVisibility pagerVisibility) { this.pagerVisibility = pagerVisibility; }
}