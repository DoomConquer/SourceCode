import java.util.HashMap;
import java.util.Map;

public class HappyNumber {

	public boolean isHappy(int n) {
		if(n <= 0) return false;
		Map<Integer, Object> map = new HashMap<Integer, Object>();
		while(n >= 1){
			if(map.containsKey(n)) break;
			map.put(n, null);
			int len = 0;
			int temp = n;
			while(temp / 10 != 0){
				len++;
				temp /= 10;
			}
			len++;
			int[] num = new int[len];
			int i = 0;
			while(n / 10 != 0){
				num[i++] = n % 10;
				n /= 10;
			}
			num[i] = n;
			int sum = 0;
			for(i = 0; i < len; i++){
				sum += num[i] * num[i];
			}
			if(sum == 1) return true;
			n = sum;
		}
		return false;
	}
	
	public static void main(String[] args) {
		HappyNumber happy = new HappyNumber();
		System.out.println(happy.isHappy(18));
	}

}
