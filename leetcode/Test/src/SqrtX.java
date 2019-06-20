
public class SqrtX {

	public int mySqrt(int x) {
		int res = 0;
		for(int i = 0; i <= 46340; i++){
			if(i * i <= x) {
				res = i;
				continue;
			}
			break;
		}
		return res;
	}
	
	public static void main(String[] args) {
		SqrtX sqrt = new SqrtX();
		System.out.println(sqrt.mySqrt(100));
		System.out.println(sqrt.mySqrt(Integer.MAX_VALUE));
		System.out.println(sqrt.mySqrt(8));
	}

}
