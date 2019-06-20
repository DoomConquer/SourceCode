import java.util.ArrayList;
import java.util.List;

public class PlusOne {

	public int[] plusOne(int[] digits) {
		List<Integer> res = new ArrayList<Integer>();
		int flag = 1;
		for(int i = digits.length - 1; i >= 0; i--){
			res.add((digits[i] + flag) % 10);
			flag = (digits[i] + flag) / 10;
		}
		if(flag != 0)
			res.add(flag);
		int size = res.size();
		int[] sum = new int[size];
		for(int i = 0; i < size; i++){
			sum[size - 1 - i] = res.get(i);
		}
		return sum;
	}
	public static void main(String[] args) {
		PlusOne plus = new PlusOne();
		System.out.println(plus.plusOne(new int[]{9,9,9,9,9,9,9,9,9}));
	}

}
