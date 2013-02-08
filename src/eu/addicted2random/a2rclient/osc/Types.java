package eu.addicted2random.a2rclient.osc;

import java.util.HashMap;
import java.util.Map;

public class Types {
  
  static final private String FLOAT = "float";
  static final private String DOUBLE = "double";
  static final private String INTEGER = "integer";
  static final private String STRING = "string";
  
  static final public FloatType FLOAT_TYPE = new FloatType();
  static final public DoubleType DOUBLE_TYPE = new DoubleType();
  static final public IntegerType INTEGER_TYPE = new IntegerType();
  static final public StringType STRING_TYPE = new StringType();
  
  static final private Map<String, Type> TYPE_BY_NAME = new HashMap<String, Type>(4);
  
  static {
    TYPE_BY_NAME.put(FLOAT, FLOAT_TYPE);
    TYPE_BY_NAME.put(DOUBLE, DOUBLE_TYPE);
    TYPE_BY_NAME.put(INTEGER, INTEGER_TYPE);
    TYPE_BY_NAME.put(STRING, STRING_TYPE);
  }
  
  static public Type getTypeByName(String name) {
    return TYPE_BY_NAME.get(name);
  }
  
}
