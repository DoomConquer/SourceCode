
public class ConvertSortedArraytoBinarySearchTree {

	public TreeNode sortedArrayToBST(int[] nums) {
		if(nums == null) return null;
		return construct(nums, 0, nums.length - 1);
	}
	private TreeNode construct(int[] nums, int left, int right){
		if(left > right) return null;
		int mid = (left + right) >>> 1;
		TreeNode root = new TreeNode(nums[mid]);
		root.left = construct(nums, left, mid - 1);
		root.right = construct(nums, mid + 1, right);
		return root; 
	}
	
	public static void main(String[] args) {
		ConvertSortedArraytoBinarySearchTree tree = new ConvertSortedArraytoBinarySearchTree();
		tree.sortedArrayToBST(new int[]{-10,-3,0,5,9});
	}

}
