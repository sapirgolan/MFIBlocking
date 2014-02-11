package fimEntityResolution;

import java.lang.instrument.Instrumentation;

public class InstrumentationAgent {

	 private static volatile Instrumentation globalInstr;
	  public static void premain(String args, Instrumentation inst) {
		  System.out.println("premain called!!");
	    globalInstr = inst;
	  }
	  public static long getObjectSize(Object obj) {
	    if (globalInstr == null)
	      throw new IllegalStateException("Agent not initted");
	    return globalInstr.getObjectSize(obj);
	  }

}
