import java.util.ArrayList;
import java.util.List;

public class FindKClosestElements {

    public List<Integer> findClosestElements(int[] arr, int k, int x) {
        List<Integer> list = new ArrayList<Integer>();
        int left = 0, right = arr.length - k;
        while(left < right){ // 二分找到k小的元素起始位置
        	int mid = left + (right - left) / 2;
        	if(x - arr[mid] > arr[mid + k] - x) left = mid + 1;
        	else right = mid;
        }
        while(k-- > 0) list.add(arr[left++]);
        return list;
    }
    
	public static void main(String[] args) {
		FindKClosestElements FindKClosestElements = new FindKClosestElements();
		System.out.println(FindKClosestElements.findClosestElements(new int[]{1,2,3,4,5}, 4, 3));
		System.out.println(FindKClosestElements.findClosestElements(new int[]{1,2,3,4,5}, 4, -1));
	}

}
