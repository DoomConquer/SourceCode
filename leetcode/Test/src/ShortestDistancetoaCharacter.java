import java.util.ArrayList;
import java.util.List;

public class ShortestDistancetoaCharacter {

	public int[] shortestToChar(String S, char C) {
		List<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < S.length(); i++)
			if(S.charAt(i) == C) list.add(i);
		int[] res = new int[S.length()];
		for(int i = 0, j = 0; i < S.length(); i++){
			if(i >= list.get(j) && j + 1 < list.size()) j++;
			int pre = Integer.MAX_VALUE;
			if(j - 1 >= 0) pre = list.get(j - 1);
			res[i] = Math.min(Math.abs(list.get(j) - i), Math.abs(pre - i));
		}
		return res;
	}
	
	public static void main(String[] args) {
		ShortestDistancetoaCharacter shortest = new ShortestDistancetoaCharacter();
		int[] res = shortest.shortestToChar("loveleetcodem", 'm');
		for(int num : res)
			System.out.print(num + "  ");
	}

}
