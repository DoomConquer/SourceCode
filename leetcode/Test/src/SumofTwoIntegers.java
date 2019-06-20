
public class SumofTwoIntegers {

	public int getSum(int a, int b) {
		if(b == 0) return a;
		int carray = (a & b) << 1; // 所有进位求和
		int sum = a ^ b;           // 没有进位的求和
		return getSum(sum, carray);
	}
	
	public static void main(String[] args) {
		SumofTwoIntegers sum = new SumofTwoIntegers();
		System.out.println(sum.getSum(10, 18));
	}

}
