import java.util.ArrayList;
import java.util.List;

public class ValidateBinarySearchTree {

	public boolean isValidBST(TreeNode root) {
		List<Integer> res = new ArrayList<>();	
		traverseBST(root, res);
		for(int i = 1; i < res.size(); i++)
			if(res.get(i) <= res.get(i - 1)) return false;
		return true;
	}
	private void traverseBST(TreeNode root, List<Integer> res){
		if(root == null) return;
		traverseBST(root.left, res);
		res.add(root.val);
		traverseBST(root.right, res);
	}
	
	public static void main(String[] args) {
		ValidateBinarySearchTree tree = new ValidateBinarySearchTree();
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
		System.out.println(tree.isValidBST(root));
	}

}
