import java.util.ArrayList;
import java.util.List;

public class GrayCode {

	public List<Integer> grayCode(int n) {
		List<Integer> res = new ArrayList<Integer>();
		gray(res, n);
		return res;
	}
	int num = 0;
	private void gray(List<Integer> res, int n){
		if(n == 0){
			res.add(num);
			return;
		}
		gray(res, n - 1);
		num ^= (1 << n -1);
		gray(res, n - 1);
	}
	
	public static void main(String[] args) {
		GrayCode gray = new GrayCode();
		System.out.println(gray.grayCode(2));
	}

}
