import java.util.HashMap;
import java.util.Map;

public class FractiontoRecurringDecimal {

	public String fractionToDecimal(int numerator, int denominator) {
		if(denominator == 0) return "";
		if(numerator == 0) return "0";
		StringBuilder sb = new StringBuilder();
		if(((numerator >>> 31) ^ (denominator >>> 31)) == 1) sb.append("-");
		long num = Math.abs((long)numerator);
		long den = Math.abs((long)denominator);
		Map<Long, Integer> map = new HashMap<>();
		sb.append(num / den);
		long mod = num % den;
		if(mod == 0) return sb.toString();
		sb.append(".");
		while(mod != 0 && !map.containsKey(mod)){
			map.put(mod, sb.length());
			mod *= 10;
			long res = mod / den;
			sb.append(res);
			mod %= den;
		}
		if(mod != 0)
			sb.insert(map.get(mod), "(").append(")");
		return sb.toString();
	}
	
	public static void main(String[] args) {
		FractiontoRecurringDecimal fraction = new FractiontoRecurringDecimal();
		System.out.println(fraction.fractionToDecimal(1, 2));
		System.out.println(fraction.fractionToDecimal(2, 3));
		System.out.println(fraction.fractionToDecimal(7, 1));
		System.out.println(fraction.fractionToDecimal(1, 17));
		System.out.println(fraction.fractionToDecimal(1, 19));
		System.out.println(fraction.fractionToDecimal(1, 77));
		System.out.println(fraction.fractionToDecimal(1, 11));
		System.out.println(fraction.fractionToDecimal(1, 7));
		System.out.println(fraction.fractionToDecimal(70, 3));
		System.out.println(fraction.fractionToDecimal(100, 5));
		System.out.println(fraction.fractionToDecimal(100, 1000));
		System.out.println(fraction.fractionToDecimal(4, 9));
		System.out.println(fraction.fractionToDecimal(4, 333));
		System.out.println(fraction.fractionToDecimal(4, -333));
		System.out.println(fraction.fractionToDecimal(4, 0));
		System.out.println(fraction.fractionToDecimal(0, 4));
		System.out.println(fraction.fractionToDecimal(-50, 8));
		System.out.println(fraction.fractionToDecimal(-22, -2));
		System.out.println(fraction.fractionToDecimal(-1, -2147483648));
	}

}
