
public class RangeSumQueryMutable {

	public static void main(String[] args) {
		int[] num = new int[]{1,2,1,1,2,3,4,2};
		NumArray numArray = new NumArray(num);
		System.out.println(numArray.sumRange(0, 3));
		System.out.println(numArray.sumRange(0, 0));
		System.out.println(numArray.sumRange(1, 3));
		numArray.update(1, 10);
		System.out.println(numArray.sumRange(0, 3));
		System.out.println(numArray.sumRange(0, 0));
		System.out.println(numArray.sumRange(1, 1));
		System.out.println(numArray.sumRange(1, 3));
	}

}

/**
 * @author li_zhe
 * BIT树状数组方法
 */
//class NumArray {
//
//	private int[] bitTree, nums;
//    public NumArray(int[] nums) {
//    	this.nums = new int[nums.length];
//        this.bitTree = new int[nums.length + 1];
//        for(int i = 0; i < nums.length; i++)
//        	update(i, nums[i]);
//    }
//    
//	public void update(int i, int val) {
//		i++;
//		int temp = val - nums[i - 1];
//		nums[i - 1] = val;
//		while(i < bitTree.length){
//			bitTree[i] += temp;
//			i += lowbit(i);
//		}
//    }
//    
//	private int sum(int i){
//		i++;
//		int sum = 0;
//		while(i > 0){
//			sum += bitTree[i];
//			i -= lowbit(i);
//		}
//		return sum;
//	}
//    public int sumRange(int i, int j) {
//    	if(i == 0) return sum(j);
//        return sum(j) - sum(i - 1);
//    }
//    
//    private int lowbit(int index){
//    	return index & -index;
//    }
//}

/**
 * @author li_zhe
 * ST线段树方法
 */
class NumArray {
	class STNode{
		int start, end;
		int sum;
		STNode left,right;
		public STNode(int start, int end){
			this.start = start;
			this.end = end;
		}
	}
	private STNode buildTree(int start, int end){
		STNode root = new STNode(start, end);
		if(start == end){
			root.sum = nums[start];
			return root;
		}
		int mid = (start + end) >>> 1;
		root.left = buildTree(start, mid);
		root.right = buildTree(mid + 1, end);
		root.sum = root.left.sum + root.right.sum;
		return root;
	}
	private int updateNode(STNode root, int index, int val){
		if(index < root.start || index > root.end) return root.sum;
		if(root.start == root.end && root.start == index){
			root.sum = val;
			nums[index] = val;
			return root.sum;
		}
		root.sum = updateNode(root.left, index, val) + updateNode(root.right, index, val);
		return root.sum;
	}
	private int query(STNode root, int start, int end){
		if(end < root.start || start > root.end) return 0;
		if(start <= root.start && end >= root.end) return root.sum;
		return query(root.left, start, end) + query(root.right, start, end);
	}
	
	private int[] nums;
	private STNode root;
	public NumArray(int[] nums) {
		if(nums == null || nums.length == 0) return;
		this.nums = nums;
		root = buildTree(0, nums.length - 1);
	}
	
	public void update(int i, int val) {
		updateNode(root, i, val);
	}
	
	public int sumRange(int i, int j) {
		return query(root, i, j);
	}
}