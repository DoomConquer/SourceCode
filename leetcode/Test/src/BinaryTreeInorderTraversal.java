import java.util.ArrayList;
import java.util.List;

public class BinaryTreeInorderTraversal {

	public List<Integer> inorderTraversal(TreeNode root) {
		List<Integer> res = new ArrayList<>();
		inorder(root, res);
		return res;
	}
	private void inorder(TreeNode root, List<Integer> res){
		if(root == null) return;
		inorder(root.left, res);
		res.add(root.val);
		inorder(root.right, res);
	}
	
	public static void main(String[] args) {
		BinaryTreeInorderTraversal tree = new BinaryTreeInorderTraversal();
		TreeNode root = new TreeNode(2);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(3);
		TreeNode node5 = new TreeNode(5);
		TreeNode node6 = new TreeNode(6);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node2.left = node5;
		node2.right = node6;
		System.out.println(tree.inorderTraversal(root));
	}

}
