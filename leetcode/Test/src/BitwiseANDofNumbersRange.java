
/**
 * @author li_zhe
 * 参考leetcode思路：找出两个数最高位不相同的位，表示之前的地位是慢慢加上来的，所以与运算都为0
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
