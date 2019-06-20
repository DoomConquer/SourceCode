import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class RelativeRanks {

    public String[] findRelativeRanks(int[] nums) {
        String[] res = new String[nums.length];
        TreeMap<Integer, Integer> map = new TreeMap<>(Collections.reverseOrder());
        for(int i = 0; i < nums.length; i++) map.put(nums[i], i);
        int index = 0, count = 0;
        for(Map.Entry<Integer, Integer> entry : map.entrySet()){
        	if(index < 3){
        		switch(index){
        		case 0:
        			res[entry.getValue()] = "Gold Medal";
        			break;
        		case 1:
        			res[entry.getValue()] = "Silver Medal";
        			break;
        		case 2:
        			res[entry.getValue()] = "Bronze Medal";
        			break;
        		}
        		index++; count++; continue;
        	}
        	count++;
        	res[entry.getValue()] = String.valueOf(count);
        }
        return res;
    }
    
	public static void main(String[] args) {
		RelativeRanks relativeRanks = new RelativeRanks();
		System.out.println(Arrays.toString(relativeRanks.findRelativeRanks(new int[]{5, 4, 3, 2, 1})));
		System.out.println(Arrays.toString(relativeRanks.findRelativeRanks(new int[]{1, 2, 3, 4, 5})));
		System.out.println(Arrays.toString(relativeRanks.findRelativeRanks(new int[]{1, 2})));
	}

}
