package org.pm4j.core.pm.impl;

import java.math.RoundingMode;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttrDouble;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrDoubleCfg;
import org.pm4j.core.pm.impl.converter.PmConverterDouble;

/**
 * Implements a PM attribute for {@link Double} values.
 *
 * @author olaf boede
 */
public class PmAttrDoubleImpl extends PmAttrNumBase<Double> implements PmAttrDouble {

  public PmAttrDoubleImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Interface implementation ======== //

  public Double getMax() {
    return getOwnMetaDataWithoutPmInitCall().maxValue;
  }

  public Double getMin() {
    return getOwnMetaDataWithoutPmInitCall().minValue;
  }

  // ======== Value handling ======== //

  /**
   * The default format key {@link #RESKEY_DEFAULT_FLOAT_FORMAT_PATTERN} applies when no
   * special format is defined (either by resource key with postfix or
   * annotation).
   *
   * @see PmAttrBase#getFormatString()
   */
  @Override
  protected String getFormatDefaultResKey() {
    return RESKEY_DEFAULT_FLOAT_FORMAT_PATTERN;
  }

  @Override
  protected void validate(Double value) throws PmValidationException {
    super.validate(value);

    if (value != null) {
      double v = value.doubleValue();
      if (v < getMin().doubleValue()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_LOW, getMin());
      }
      if (v > getMax().doubleValue()) {
        throw new PmValidationException(this, PmConstants.MSGKEY_VALIDATION_VALUE_TOO_HIGH, getMax());
      }
    }
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;
    myMetaData.setConverterDefault(PmConverterDouble.INSTANCE);
    
    PmAttrDoubleCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrDoubleCfg.class);
    if (annotation != null) {
      double maxValue = myMetaData.maxValue = annotation.maxValue();
      double minValue = myMetaData.minValue = annotation.minValue();

      if (minValue > maxValue) {
        throw new PmRuntimeException(this, "minValue(" + minValue + ") > maxValue(" + maxValue + ")");
      }
      myMetaData.stringConversionRoundingMode = annotation.stringConversionRoundingMode();
    }
  }

  protected static class MetaData extends PmAttrNumBase.MetaData {
    private double maxValue = Double.MAX_VALUE;
    private double minValue = -Double.MAX_VALUE;
    public RoundingMode stringConversionRoundingMode = ROUNDINGMODE_DEFAULT;

    @Override
    protected double getMaxValue() {
      return maxValue;
    }

  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }
  
  /**
   * @return rounding mode when converting to pm value. Changing this to a value
   *         different than RoundingMode.UNNECESSARY will allow to set more
   *         fraction digits than specified in the format. Those additional
   *         digits will then be rounded.
   */  
  public RoundingMode getStringConversionRoundingMode() {
    return getOwnMetaDataWithoutPmInitCall().stringConversionRoundingMode;
  }


}
