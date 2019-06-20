
public class OnebitandTwobitCharacters {

	public boolean isOneBitCharacter(int[] bits) {
		int start = 0;
		for(int i = 0; i < bits.length; i++){
			if(bits[i] == 1){
				start = i;
				i++;
			}else{
				start = i;
			}
			
		}
		return start == bits.length - 1 ? true : false;
	}
	
	public static void main(String[] args) {
		OnebitandTwobitCharacters isOneBit = new OnebitandTwobitCharacters();
		System.out.println(isOneBit.isOneBitCharacter(new int[]{0}));
	}

}
