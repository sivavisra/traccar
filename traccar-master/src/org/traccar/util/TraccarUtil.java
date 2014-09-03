package org.traccar.util;

import java.text.DecimalFormat;

public class TraccarUtil {

	public static String convertToDegrees(String seconds) {
		String sign = seconds.substring(seconds.length() - 1, seconds.length());
		String valStr = seconds.substring(0, seconds.length() - 1);

		double val = Double.valueOf(valStr).doubleValue();
		val /= 100.0D;
		int latd = (int) val;
		val = (val - latd) * 100.0D / 60.0D;
		double valToRet = latd + val;
		DecimalFormat df = new DecimalFormat("00.00000");
		return ((sign.equals("W")) || (sign.equals("S")) ? "-" : "")
				+ df.format(valToRet);
	}
	
	public static String roundTo5DecimalValue(Double sourceVal) {
					 
		DecimalFormat df = new DecimalFormat("###.#####");
				
		return df.format(Math.round(sourceVal*100000.0)/100000.0); 
	}
	
	public static void main(String [] args)
    {

		System.out.println(roundTo5DecimalValue(77.05578833333334));
     
    }
	
	
}
