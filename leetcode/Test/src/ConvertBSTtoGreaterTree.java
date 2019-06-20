
public class ConvertBSTtoGreaterTree {

	public TreeNode convertBST(TreeNode root) {
		postTraverse(root);
		return root;
	}
	int preSum = 0;
	private void postTraverse(TreeNode root){
		if(root == null) return;
		postTraverse(root.right);
		preSum += root.val;
		root.val = preSum;
		postTraverse(root.left);
	}
	
	public static void main(String[] args) {
		ConvertBSTtoGreaterTree convert = new ConvertBSTtoGreaterTree();
		TreeNode root = new TreeNode(5);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(13);
		root.left = node1;
		root.right = node2;
		convert.convertBST(root);
	}

}
