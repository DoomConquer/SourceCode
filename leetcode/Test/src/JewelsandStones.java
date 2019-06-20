public class JewelsandStones {
    public int numJewelsInStones(String J, String S) {
        int[] num = new int['z' + 1];
        for(int i = 0; i < S.length(); i++){
        	num[S.charAt(i)]++;
        }
        int sum = 0;
        for(int i = 0; i < J.length(); i++) sum += num[J.charAt(i)];
        return sum;
    }
    
	public static void main(String[] args) {
		JewelsandStones jewelsandStones = new JewelsandStones();
		System.out.println(jewelsandStones.numJewelsInStones("aA", "aAAbbbb"));
		System.out.println(jewelsandStones.numJewelsInStones("z", "ZZ"));
	}

}
