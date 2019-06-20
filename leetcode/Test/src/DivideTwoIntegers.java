
public class DivideTwoIntegers {

	public int divide(int dividend, int divisor) {
		boolean flag1 = dividend > 0 ? true : false;
		boolean flag2 = divisor > 0 ? true : false;
		long res = div(Math.abs((long)dividend), Math.abs((long)divisor));
		if(flag1 ^ flag2) res = -res;
		if(res > Integer.MAX_VALUE) res = Integer.MAX_VALUE;
		return (int)res;
	}
	private long div(long dividend, long divisor){
		long count = 0;
		while(dividend >= divisor){
			int muti = 1;
			long temp = divisor;
			while(dividend > (temp << 1)){
				temp <<= 1;
				muti <<= 1;
			}
			dividend -= temp;
			count += muti;
		}
		return count;
	}
	
	public static void main(String[] args) {
		DivideTwoIntegers divide = new DivideTwoIntegers();
		System.out.println(divide.divide(10, 3));
		System.out.println(divide.divide(7, -3));
		System.out.println(divide.divide(-7, -3));
		System.out.println(divide.divide(2147483647, 1));
		System.out.println(divide.divide(2147483647, 2));
		System.out.println(divide.divide(-2147483648, -1));
		System.out.println(divide.divide(-2147483648, 1));
		System.out.println(divide.divide(-1, -1));
	}

}
