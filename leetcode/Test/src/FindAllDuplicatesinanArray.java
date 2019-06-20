import java.util.ArrayList;
import java.util.List;

public class FindAllDuplicatesinanArray {

    public List<Integer> findDuplicates(int[] nums) {
        List<Integer> list = new ArrayList<Integer>();
        for(int i = 0; i < nums.length; i++){
        	int index = Math.abs(nums[i]) - 1;
        	if(nums[index] > 0){
        		nums[index] = -nums[index];
        	}else{
        		list.add(Math.abs(nums[i]));
        	}
        }
        return list;
    }
    
	public static void main(String[] args) {
		FindAllDuplicatesinanArray FindAllDuplicatesinanArray = new FindAllDuplicatesinanArray();
		List<Integer> list = FindAllDuplicatesinanArray.findDuplicates(new int[]{4,3,2,7,8,2,3,1});
		for(int num : list) System.out.print(num + "  ");
		list = FindAllDuplicatesinanArray.findDuplicates(new int[]{10,2,5,10,9,1,1,4,3,7});
		for(int num : list) System.out.print(num + "  ");
	}

}
