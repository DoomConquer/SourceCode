import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Triangle {

    public int minimumTotal(List<List<Integer>> triangle) {
        if(triangle == null || triangle.size() == 0) return 0;
        int[] sum = new int[triangle.size()];
        sum[0] = triangle.get(0).get(0);
        for(int i = 1; i < triangle.size(); i++){
        	List<Integer> list = triangle.get(i);
        	int temp = 0, pre = 0;
        	for(int j = 0; j < list.size(); j++){
        		temp = sum[j];
        		if(j == 0){
        			sum[j] = sum[j] + list.get(j);
        		}else if(j > 0 && j < triangle.get(i - 1).size()){
        			sum[j] = Math.min(list.get(j) + pre, list.get(j) + sum[j]);
        		}else{
        			sum[j] = pre + list.get(j);
        		}
        		pre = temp;
        	}
        }
        int min = Integer.MAX_VALUE;
        for(int num : sum){
        	if(num < min) min = num;
        }
        return min;
    }
    
    // o(1)空间解法（自底向上）
    public int minimumTotal2(List<List<Integer>> triangle) {
    	for(int i = triangle.size() - 2; i >= 0; i--){
    		List<Integer> list = triangle.get(i);
    		for(int j = 0; j < list.size(); j++){
    			triangle.get(i).set(j, triangle.get(i).get(j) + Math.min(triangle.get(i + 1).get(j), triangle.get(i + 1).get(j + 1)));
    		}
    	}
    	return triangle.get(0).get(0);
    }
    
	public static void main(String[] args) {
		Triangle Triangle = new Triangle();
		List<List<Integer>> list = new ArrayList<List<Integer>>();
		list.add(Arrays.asList(2));
		list.add(Arrays.asList(3,4));
		list.add(Arrays.asList(6,5,7));
		list.add(Arrays.asList(4,1,8,3));
		System.out.println(Triangle.minimumTotal(list));
		System.out.println(Triangle.minimumTotal2(list));
	}
}
