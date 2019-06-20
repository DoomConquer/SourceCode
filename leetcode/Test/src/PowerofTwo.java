
public class PowerofTwo {

	public boolean isPowerOfTwo(int n) {
		if(n <= 0) return false;
		int flag = 0;
		while(n > 0) { 
			if((n & 1) == 1) flag++; 
			if(flag == 2) return false;
			n >>>= 1; 
		}
		return true;
	}
	
	public static void main(String[] args) {
		PowerofTwo power = new PowerofTwo();
		System.out.println(power.isPowerOfTwo(0));
	}

}
