
/**
 * @author li_zhe
 * �ο�leetcode˼·���ҳ����������λ����ͬ��λ����ʾ֮ǰ�ĵ�λ�������������ģ����������㶼Ϊ0
 */
public class BitwiseANDofNumbersRange {

	public int rangeBitwiseAnd(int m, int n) {
		int diff = m ^ n;
		int max = 0;
		while(diff > 0){
			diff >>= 1;
			max = (max << 1) + 1;
		}
		return m & (~max);
	}
	
	public static void main(String[] args) {
		BitwiseANDofNumbersRange bit = new BitwiseANDofNumbersRange();
		System.out.println(bit.rangeBitwiseAnd(5, 7));
		System.out.println(bit.rangeBitwiseAnd(2, 6));
		System.out.println(bit.rangeBitwiseAnd(1, 8266));
		System.out.println(bit.rangeBitwiseAnd(700000000, 2147483641));
	}
	
}
