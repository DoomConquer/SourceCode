
public class NimGame {

	public boolean canWinNim(int n) {
		return n % 4 == 0 ? false : true;
	}
	
	public static void main(String[] args) {
		NimGame nim = new NimGame();
		System.out.println(nim.canWinNim(100));
		System.out.println(nim.canWinNim(4));
		System.out.println(nim.canWinNim(5));
		System.out.println(nim.canWinNim(4));
	}

}
