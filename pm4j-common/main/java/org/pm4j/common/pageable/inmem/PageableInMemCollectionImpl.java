package org.pm4j.common.pageable.inmem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.Query;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerWithItemSet;

/**
 * Implements a {@link PageableCollection} based on an {@link List} of items to
 * handle.
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 *          The type of items handled by this set.
 */
public class PageableInMemCollectionImpl<T_ITEM> extends PageableCollectionBase2<T_ITEM> implements PageableInMemCollection<T_ITEM> {

  /** The collection type specific selection handler. */
  private final SelectionHandler<T_ITEM> selectionHandler;
  /** Contains the not filtered set of items in their original sort order. */
  private Collection<T_ITEM>             originalObjects;
  /** The current set of filtered and sorted items. */
  private List<T_ITEM>                   objects;
  /** The currently active sort order comparator. */
  private Comparator<T_ITEM>             sortOrderComparator;

  private InMemQueryEvaluator<T_ITEM>    inMemQueryEvaluator;

  /** A listener gets called if a query property gets changed that affects the effective filter result. */
  private PropertyChangeListener changeFilterListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      objects = null;
    }
  };
  /** A listener gets called if a query property gets changed that affects the sort order of the items to show. */
  private PropertyChangeListener changeSortOrderListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      sortOrderComparator = null;
      objects = null;
    }
  };

  /**
   * @param objects
   *          the set of objects to iterate over.
   * @param query
   *          defines the current sort order and filter restricions.
   */
  public PageableInMemCollectionImpl(InMemQueryEvaluator<T_ITEM> inMemQueryEvaluator, Collection<T_ITEM> objects, QueryOptions queryOptions, Query query) {
    super(queryOptions, query);
    this.inMemQueryEvaluator = inMemQueryEvaluator;
    this.selectionHandler = new SelectionHandlerWithItemSet<T_ITEM>(this);
    this.originalObjects = (objects != null)
        ? objects
        : new ArrayList<T_ITEM>();

    // getQuery is used because the super ctor may have created it on the fly (in case of a null parameter)
    getQuery().addPropertyChangeListener(Query.PROP_EFFECTIVE_FILTER, changeFilterListener);
    getQuery().addPropertyChangeListener(Query.PROP_EFFECTIVE_SORT_ORDER, changeSortOrderListener);
  }

  @Override
  public List<T_ITEM> getItemsOnPage() {
    List<T_ITEM> objects = _getObjects();
    if (objects.isEmpty()) {
      return Collections.emptyList();
    }

    int first = PageableCollectionUtil2.getIdxOfFirstItemOnPageAsInt(this) - 1;
    int last = PageableCollectionUtil2.getIdxOfLastItemOnPageAsInt(this);

    if (first > last) {
      return Collections.emptyList();
    }

    return objects.subList(first, last);
  }

  @Override
  public long getNumOfItems() {
    return _getObjects().size();
  }

  @Override
  public long getUnfilteredItemCount() {
    return originalObjects.size();
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return _getObjects().iterator();
  }

  @Override
  public SelectionHandler<T_ITEM> getSelectionHandler() {
    return selectionHandler;
  }

  @Override
  public void clearCaches() {
    sortOrderComparator = null;
    objects = null;
  }

  @Override
  public void setSortOrderComparator(Comparator<T_ITEM> comparator) {
    if (comparator != this.sortOrderComparator) {
      sortOrderComparator = comparator;
      objects = null;
    }
  }

  private Comparator<T_ITEM> _getSortOrderComparator() {
    if (sortOrderComparator == null) {
      sortOrderComparator = inMemQueryEvaluator.getComparator(getQuery().getEffectiveSortOrder());
    }
    return sortOrderComparator;
  }

  private List<T_ITEM> _getObjects() {
    if (objects == null) {
      if (originalObjects.isEmpty()) {
        objects = Collections.emptyList();
      }
      else {
        List<T_ITEM> list = _filter(new ArrayList<T_ITEM>(originalObjects));
        Comparator<T_ITEM> comparator = _getSortOrderComparator();

        if (comparator != null) {
          Collections.sort(list, comparator);
        }

        objects = list;
      }

      // XXX olaf: just moves to the last possible page if necessary.
      // The user may want to stay on the page with his selected item.
      // We need to define strategies for application specific definitions.
      if (getCurrentPageIdx() * getPageSize() >= objects.size()) {
        setCurrentPageIdx(PageableCollectionUtil2.getNumOfPages(this));
      }

      // TODO olaf: if the filter gets changed to find items again, we need to do this to prevent
      //            negative page item index identification...
      // Check if it was really a good idea to have a 0-page index for empty collections.
      if (objects.size() > 0 && getCurrentPageIdx() == 0) {
        setCurrentPageIdx(1);
      }
    }

    return objects;
  }

  /** Generates a list of filtered items based on the given list. */
  private List<T_ITEM> _filter(List<T_ITEM> unfilteredList) {
    FilterExpression filterExpression = getQuery().getFilterExpression();
    if (filterExpression == null) {
      return unfilteredList;
    }

    List<T_ITEM> filteredList = inMemQueryEvaluator.evaluateSubSet(unfilteredList, filterExpression);
    return filteredList;
  }

}
