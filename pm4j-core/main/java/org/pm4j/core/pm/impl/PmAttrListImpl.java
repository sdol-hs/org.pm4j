package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrList;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrListCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.impl.converter.PmConverterInteger;
import org.pm4j.core.pm.impl.converter.PmConverterList;
import org.pm4j.core.pm.impl.converter.PmConverterLong;
import org.pm4j.core.pm.impl.converter.PmConverterString;
import org.pm4j.core.util.reflection.ClassUtil;

public class PmAttrListImpl<T> extends PmAttrBase<List<T>, Collection<T>> implements PmAttrList<T> {

  @PmAttrListCfg(itemConverter=PmConverterLong.class)
  public static class PmAttrListOfLongs extends PmAttrListImpl<Long> {
    public PmAttrListOfLongs(PmElementBase pmParentBean) {
      super(pmParentBean);
    }
  }

  @PmAttrListCfg(itemConverter=PmConverterInteger.class)
  public static class PmAttrListOfIntegers extends PmAttrListImpl<Integer> {
    public PmAttrListOfIntegers(PmElementBase pmParentBean) {
      super(pmParentBean);
    }
  }

  @PmAttrListCfg(itemConverter=PmConverterString.class)
  public static class PmAttrListOfStrings extends PmAttrListImpl<String> {
    public PmAttrListOfStrings(PmElementBase pmParentBean) {
      super(pmParentBean);
    }
  }

  public PmAttrListImpl(PmObject pmParent) {
    super(pmParent);
  }

  public List<T> getValueAsList() {
    return getValue();
  }

  public void setValueAsList(List<T> value) {
    setValue(value);
  }

  @Override
  public List<String> getValueAsStringList() {
    List<T> items = getValue();
    if (items != null) {
      Converter<T> c = getItemConverter();
      List<String> stringList = new ArrayList<String>(items.size());
      for (T item : items) {
        stringList.add(c.valueToString(this, item));
      }
      return stringList;
    }
    else {
      return Collections.emptyList();
    }
  }

  @Override @SuppressWarnings("unchecked")
  public void setValueAsStringList(List<String> value) throws PmConverterException {
    if (value != null) {
      Converter<T> c = getItemConverter();
      List<T> items = new ArrayList<T>(value.size());
      for (String s : value) {
        items.add(c.stringToValue(this, s));
      }
      setValue(items);
    }
    else {
      setValue(Collections.EMPTY_LIST);
    }
  }

  /**
   * The item-{@link Converter} can be configured using the annotation
   * {@link PmAttrListCfg#itemConverter()} or by overriding this method.
   *
   * @return The {@link Converter} used for the list item values.
   */
  protected Converter<T> getItemConverter() {
    @SuppressWarnings("unchecked")
    Converter<T> c = (Converter<T>)getOwnMetaData().itemConverter;
    if (c == null) {
      throw new PmRuntimeException(this, "Missing item value converter.");
    }
    return c;
  }

  @Override
  protected boolean isEmptyValue(List<T> value) {
    return (value == null) || value.isEmpty();
  }

  @Override
  public List<T> getValueSubset(int fromIdx, int numItems) {
    List<T> all = getValue();
    int toRow = (numItems == -1)
        ? all.size()
        : Math.min(fromIdx+numItems, all.size());

    List<T> subList = all.subList(fromIdx, toRow);
    return subList;
  }

  @Override
  public int getSize() {
    List<T> value = getValue();
    return (value != null) ? value.size() : 0;
  }

  @Override
  public NullOption getNullOptionDefault() {
    return NullOption.NO;
  }

  @Override
  public int getMaxLen() {
    // XXX olaf: check for a real restriction...
    return Short.MAX_VALUE;
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override @SuppressWarnings("unchecked")
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    PmAttrListCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrListCfg.class);
    if (annotation != null) {
      if (annotation.itemConverter() != Void.class) {
        myMetaData.itemConverter = ClassUtil.newInstance(annotation.itemConverter());
      }
      myMetaData.setConverter(new PmConverterList<T>((Converter<T>)myMetaData.itemConverter));
    }
  }

  protected static class MetaData extends PmAttrBase.MetaData {
    private Converter<?> itemConverter;

    @Override public Converter<?> getItemConverter()                   {  return itemConverter;   }
    @Override public void setItemConverter(Converter<?> itemConverter) {  this.itemConverter = itemConverter;    }
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}

