package com.kyoko.rpccall;

interface Valuable
{
  static final int ISIVALUE = 1;
  static final int ISRVALUE = 2;
  static final int ISSVALUE = 3;
  static final int ISBVALUE = 4;
  static final int ISDVALUE = 8;
  static final int ISBUFVALUE = 9;
  static final int ISNULVALUE = 10;
  static final int ISTVALUE = 11;
  static final int ISTSVALUE = 12;

  static final int V_INTEGER = 1;
  static final int V_LONG = 2;
  static final int V_FLOAT = 3;
  static final int V_STRING = 4;
  static final int V_DATE = 5;
  static final int V_BUFFER = 6;
  static final int V_NULL = 7;
  static final int V_BYTEARR = 8;
  static final int V_TOKEN = 9;
  static final int V_STRING_WITH_TOKEN = 10;
  static final int V_BVALUE = 11;
  static final int V_LONGSTR = 12;
}
