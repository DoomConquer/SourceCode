
public class BinaryTreeMaximumPathSum {

	public int maxPathSum(TreeNode root) {
		long currVal = postOrder(root);
		return (int) Math.max(max, currVal);
	}
	int max = Integer.MIN_VALUE;
	private long postOrder(TreeNode root){
		if(root.left == null && root.right == null) return root.val;
		long currVal = 0;
		if(root.left != null && root.right != null){
			long leftVal = postOrder(root.left);
			long rightVal = postOrder(root.right);
			currVal = max(root.val, root.val + leftVal, root.val + rightVal);
			max = (int) max(max, currVal, leftVal, rightVal, root.val + leftVal + rightVal);
		}else if(root.left != null){
			long leftVal = postOrder(root.left);
			currVal = max(root.val, root.val + leftVal);
			max = (int) max(max, currVal, leftVal);
		}else{
			long rightVal = postOrder(root.right);
			currVal = max(root.val, root.val + rightVal);
			max = (int) max(max, currVal, rightVal);
		}
		return currVal;
	}
	private long max(long... val){
		long max = val[0];
		for(int i = 0; i < val.length; i++){
			max = Math.max(max, val[i]);
		}
		return max;
	}
	
	public static void main(String[] args) {
		BinaryTreeMaximumPathSum tree = new BinaryTreeMaximumPathSum();
		TreeNode root = new TreeNode(-1);
		TreeNode node1 = new TreeNode(5);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(-3);
		TreeNode node5 = new TreeNode(5);
		TreeNode node6 = new TreeNode(6);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node2.left = node5;
		node2.right = node6;
		System.out.println(tree.maxPathSum(root));
	}

}
