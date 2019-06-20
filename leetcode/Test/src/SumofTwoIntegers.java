
public class SumofTwoIntegers {

	public int getSum(int a, int b) {
		if(b == 0) return a;
		int carray = (a & b) << 1; // ���н�λ���
		int sum = a ^ b;           // û�н�λ�����
		return getSum(sum, carray);
	}
	
	public static void main(String[] args) {
		SumofTwoIntegers sum = new SumofTwoIntegers();
		System.out.println(sum.getSum(10, 18));
	}

}
