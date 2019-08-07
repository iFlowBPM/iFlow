package pt.iflow.api.processtype;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;


public final class RawNumberFormatter extends Format {
  /**
   * 
   */
  private static final long serialVersionUID = -8473832928288568062L;
  
  private static final int SIGNALSIZE = 1;
  
  private final int integerPartSize;
  private final int decimalPartSize;
  private final int numberLength;
  private final int power;
  private final NumberFormat integerFormat;
  
  public RawNumberFormatter() {
    this(15,5);
  }
  
  public RawNumberFormatter(final int integerPartSize, final int decimalPartSize) {
    this.integerPartSize = integerPartSize;
    this.decimalPartSize = decimalPartSize;
    this.numberLength = SIGNALSIZE+this.integerPartSize+this.decimalPartSize;
    this.power = (int) Math.pow(10, this.decimalPartSize);
    
    DecimalFormat df = new DecimalFormat("'1'0;'0'0");
    df.setDecimalSeparatorAlwaysShown(false);
    df.setMultiplier(this.power);
    df.setMaximumIntegerDigits(this.integerPartSize+this.decimalPartSize);
    df.setMinimumIntegerDigits(this.integerPartSize+this.decimalPartSize);
    df.setMinimumFractionDigits(0);
    df.setMaximumFractionDigits(0);
    df.setGroupingUsed(false);
    this.integerFormat = df;
  }
  
  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    if(obj == null || !(obj instanceof Number)) throw new IllegalArgumentException("Input object is not a number instance");
    this.integerFormat.format(obj, toAppendTo, pos);
    return toAppendTo;
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    if(null == source) {
      pos.setErrorIndex(0);
      return null;
    }
    if(source.length() != numberLength) {
      pos.setErrorIndex(0);
      return null;
    }
    return this.integerFormat.parse(source, pos);
  }
  
  public final double scaleValue() {
    return power;
  }
  
  public final long power() {
    return power;
  }
  
}
