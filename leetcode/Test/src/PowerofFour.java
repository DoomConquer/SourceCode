
public class PowerofFour {

	public boolean isPowerOfFour(int num) {
		if(num <= 0) return false;
		int count = 0;
		for(int i = 0; i < 32; i++){
			if(i % 2 == 1 && (num >> i & 1) == 1) return false;
			if((num >> i & 1) == 1) count++;
		}
		return count == 1;
	}
	
	public static void main(String[] args) {
		PowerofFour power = new PowerofFour();
		System.out.println(power.isPowerOfFour(0));
	}

}
