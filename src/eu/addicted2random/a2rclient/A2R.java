package eu.addicted2random.a2rclient;

import android.os.Build;

public class A2R {

  static public final String NAME = "A2R Client";
  
  static public final String VERSION = "0.0.1";
  
  static public final String USER_AGENT = String.format("%s %s (%s - %s)", NAME, VERSION, Build.MODEL, Build.VERSION.RELEASE);
}
