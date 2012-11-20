package org.pm4j.core.pm.impl.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrBigDecimal;

public class PmConverterBigDecimal extends PmConverterNumber<BigDecimal> {

  public static final PmConverterBigDecimal INSTANCE = new PmConverterBigDecimal();

  public PmConverterBigDecimal() {
    super(BigDecimal.class);
    setDefaultPattern("#0.00");
  }

  @Override
  protected NumberFormat getNumberFormat(Locale locale, String formatString, PmAttr<?> pmAttr) {
    PmAttrBigDecimal pmAttrBigDecimal = (PmAttrBigDecimal) pmAttr;
    DecimalFormat decimalFormat = new DecimalFormat(formatString, new DecimalFormatSymbols(locale));
    decimalFormat.setRoundingMode(pmAttrBigDecimal.getStringConversionRoundingMode());
    decimalFormat.setParseBigDecimal(true);
    return decimalFormat;
  }

}