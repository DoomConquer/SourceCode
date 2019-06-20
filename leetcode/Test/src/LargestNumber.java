import java.util.Arrays;
import java.util.Comparator;

public class LargestNumber {

	public String largestNumber(int[] nums) {
		String[] s = new String[nums.length];
		for(int i = 0; i < nums.length; i++){
			s[i] = String.valueOf(nums[i]);
		}
		Arrays.sort(s, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				return -(o1 + o2).compareTo(o2 + o1);
			}});
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < nums.length; i++){
			if(i == 0 && s[i].equals("0")){
				sb.append(s[i]);
				break;
			}
			sb.append(s[i]);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		LargestNumber number = new LargestNumber();
		System.out.println(number.largestNumber(new int[]{3,30,34,5,9}));
		System.out.println(number.largestNumber(new int[]{10,2,0}));
		System.out.println(number.largestNumber(new int[]{10,1,100}));
		System.out.println(number.largestNumber(new int[]{128,12}));
		System.out.println(number.largestNumber(new int[]{121,12}));
		System.out.println(number.largestNumber(new int[]{0,0}));
		System.out.println(number.largestNumber(new int[]{0,0,0,0,0,0}));
		System.out.println(number.largestNumber(new int[]{26,33,19,29,61,66,52,37,7,76,89,93,72,2,82,11,9,41,47,76,80,28,86,30,99,25,99,85,96,98,88,33,4,94,25,80,19,55,82,71,29,61,15,2,57,98,15,91,27,95,47,38,66,2,78,26,77,33,23,90,73,27,20,5,38,23,35,29,13,46,6,71,58,37,89,28,8,1,8,73,81,83,77,22,63,36,62,89,94,43,25,86,53,21,94,9,40,19,24,21}));
	}

}
