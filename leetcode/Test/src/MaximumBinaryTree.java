public class MaximumBinaryTree {

    public TreeNode constructMaximumBinaryTree(int[] nums) {
        return construct(nums, 0, nums.length - 1);
    }
    private TreeNode construct(int[] nums, int left, int right){
    	if(left > right) return null;
    	int index = left, max = nums[left];
    	for(int i = left + 1; i <= right; i++){
    		if(nums[i] > max){
    			index = i;
    			max = nums[i];
    		}
    	}
    	TreeNode root = new TreeNode(max);
    	root.left = construct(nums, left, index - 1);
    	root.right = construct(nums, index + 1, right);
    	return root;
    }
    
	public static void main(String[] args) {
		MaximumBinaryTree maximumBinaryTree = new MaximumBinaryTree();
		System.out.println(maximumBinaryTree.constructMaximumBinaryTree(new int[]{3,2,1,6,0,5}).val);
		System.out.println(maximumBinaryTree.constructMaximumBinaryTree(new int[]{3,0,5}).val);
	}

}
