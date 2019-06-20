import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistantBarcodes {

    public int[] rearrangeBarcodes(int[] barcodes) {
    	int length = barcodes.length;
        int[] res = new int[length];
        Map<Integer, Integer> map = new HashMap<>();
        for(int num : barcodes){
        	map.put(num, map.getOrDefault(num, 0) + 1);
        }
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> { return o2.getValue() - o1.getValue(); });
        int index = 0;
        for(Map.Entry<Integer, Integer> entry : list){
        	int count = entry.getValue();
        	while(count-- > 0){
	        	if(index >= length){
	        		index = 1;
	        	}
	        	res[index] = entry.getKey();
	        	index += 2;
        	}
        }
        return res;
    }
    
	public static void main(String[] args) {
		DistantBarcodes distantBarcodes = new DistantBarcodes();
		System.out.println(Arrays.toString(distantBarcodes.rearrangeBarcodes(new int[]{2,2,1,3})));
		System.out.println(Arrays.toString(distantBarcodes.rearrangeBarcodes(new int[]{1,1,1,2,2,2})));
		System.out.println(Arrays.toString(distantBarcodes.rearrangeBarcodes(new int[]{1,1,1,1,2,2,3,3})));
	}

}
