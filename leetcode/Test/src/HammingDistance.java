
public class HammingDistance {

	public int hammingDistance(int x, int y) {
		int z = x^y;
		int sum = 0;
		for(; z > 0; sum++){
			z &= z-1;
		}
		return sum;
	}
	
	public static void main(String[] args) {
		HammingDistance distance = new HammingDistance();
		System.out.println(distance.hammingDistance(1, 4));
	}

}
