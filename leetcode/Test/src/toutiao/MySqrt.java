package toutiao;

public class MySqrt {

    public int mySqrt(int x) {
        int left = 0, right = x;
        while(left <= right){
        	int mid = left + (right - left) / 2;
        	long res = (long) mid * mid;
        	if(res > x) right = mid - 1;
        	else if(res < x) left = mid + 1;
        	else return mid;
        }
        return left - 1;
    }
    
	public static void main(String[] args) {
		MySqrt mySqrt = new MySqrt();
		System.out.println(mySqrt.mySqrt(10));
		System.out.println(mySqrt.mySqrt(8));
		System.out.println(mySqrt.mySqrt(4));
		System.out.println(mySqrt.mySqrt(1));
		System.out.println(mySqrt.mySqrt(0));
		System.out.println(mySqrt.mySqrt(16));
		System.out.println(mySqrt.mySqrt(17));
		System.out.println(mySqrt.mySqrt(24));
		System.out.println(mySqrt.mySqrt(25));
		System.out.println(mySqrt.mySqrt(2147395599));
	}

}
