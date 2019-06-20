package other;

public class Heap {
	private void buildHeap(int[] nums){
		for (int i = (nums.length - 1) / 2; i >= 0; i--)
			adjustHeap(nums, i, nums.length - 1);
	}
	private void adjustHeap(int[] nums, int i, int end){
		int temp = nums[i];
		int j = 2 * i + 1;
		while(j <= end){
			if(j + 1 <= end && nums[j + 1] < nums[j])
				j++;
			if(nums[j] >= temp) break;
			nums[i] = nums[j];
			i = j;
			j = 2 * i + 1;
		}
		nums[i] = temp;
	}
	public void heapSort(int[] nums){
		buildHeap(nums);
		for(int i = nums.length - 1; i >= 1; i--){
			int temp = nums[i];
			nums[i] = nums[0];
			nums[0] = temp;
			adjustHeap(nums, 0, i - 1);
		}
		for(int num : nums)
			System.out.print(num + "  ");
		System.out.println();
	}
	
	public static void main(String[] args) {
		Heap heap = new Heap();
		heap.heapSort(new int[]{3,1,2,5,3,2,5,7,9,0,1,6,0,2,1,10,11});
		heap.heapSort(new int[]{3,1,2});
		heap.heapSort(new int[]{3,1});
		heap.heapSort(new int[]{1});
		heap.heapSort(new int[]{3,1,2,2,1,0,5});
		heap.heapSort(new int[]{3,1,2,1,1,1,2,2,2,4,5,6,11,0,10});
		
		System.out.println();
		heap.heapSort1(new int[]{3,1,2,5,3,2,5,7,9,0,1,6,0,2,1,10,11});
		heap.heapSort1(new int[]{3,1,2});
		heap.heapSort1(new int[]{3,1});
		heap.heapSort1(new int[]{1});
		heap.heapSort1(new int[]{3,1,2,2,1,0,5});
		heap.heapSort1(new int[]{3,1,2,1,1,1,2,2,2,4,5,6,11,0,10});
	}
	
	// 注锟斤拷锟铰标，index锟斤拷0锟斤拷始锟侥革拷锟斤拷锟斤拷锟斤拷为(index - 1) / 2锟斤拷锟斤拷锟絠ndex锟斤拷1锟斤拷始锟斤拷锟姐，锟斤拷么锟斤拷锟斤拷锟斤拷锟斤拷锟轿�(index - 1 - 1) / 2锟斤拷锟斤拷index / 2 - 1
	private void buildHeap1(int[] nums){
		for (int i = nums.length / 2 - 1; i >= 0; i--)
			adjustHeap1(nums, i, nums.length);
	}
	private void adjustHeap1(int[] nums, int i, int end){
		int temp = nums[i];
		int j = 2 * i + 1;
		while(j < end){ // 锟斤拷锟斤拷卤锟斤拷锟斤拷锟斤拷槌わ拷龋锟斤拷锟斤拷芗拥锟斤拷锟�
			if(j + 1 < end && nums[j + 1] < nums[j])
				j++;
			if(nums[j] >= temp) break;
			nums[i] = nums[j];
			i = j;
			j = 2 * i + 1;
		}
		nums[i] = temp;
	}
	public void heapSort1(int[] nums){
		buildHeap1(nums);
		for(int i = nums.length - 1; i >= 1; i--){
			int temp = nums[i];
			nums[i] = nums[0];
			nums[0] = temp;
			adjustHeap1(nums, 0, i);
		}
		for(int num : nums)
			System.out.print(num + "  ");
		System.out.println();
	}
}
