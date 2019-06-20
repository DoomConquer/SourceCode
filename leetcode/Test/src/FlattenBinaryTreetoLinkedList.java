
public class FlattenBinaryTreetoLinkedList {

	public void flatten(TreeNode root) {
		if(root == null) return;
		TreeNode left = root.left;
		TreeNode right = root.right;
		root.left = null;
		if(preRight != null){
			preRight.right = root;
			preRight = preRight.right;
		}else{
			preRight = root;
		}
		flatten(left);
		flatten(right);
	}
	TreeNode preRight = null;
	
	public static void main(String[] args) {
		FlattenBinaryTreetoLinkedList tree = new FlattenBinaryTreetoLinkedList();
		TreeNode root = new TreeNode(5);
		TreeNode node1 = new TreeNode(4);
		TreeNode node2 = new TreeNode(8);
		TreeNode node3 = new TreeNode(11);
		TreeNode node4 = new TreeNode(13);
		TreeNode node5 = new TreeNode(4);
		TreeNode node6 = new TreeNode(7);
		TreeNode node7 = new TreeNode(2);
		TreeNode node8 = new TreeNode(1);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node2.left = node4;
		node2.right = node5;
		node3.left = node6;
		node3.right = node7;
		node5.right = node8;
		tree.flatten(root);
		while(root != null){
			System.out.print(root.val + "  ");
			root = root.right;
		}
	}

}
