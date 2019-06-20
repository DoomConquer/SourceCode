import java.util.ArrayList;
import java.util.List;

public class BinaryTreePaths {

	public List<String> binaryTreePaths(TreeNode root) {
		List<String> res = new ArrayList<>();
		traverse(root, res, new ArrayList<>());
		return res;
	}
	private void traverse(TreeNode root, List<String> res, List<Integer> list){
		if(root == null) return;
		list.add(root.val);
		if(root.left == null && root.right == null){
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < list.size(); i++){
				if(i < list.size() - 1) sb.append(list.get(i) + "->");
				else sb.append(list.get(i));
			}
			res.add(sb.toString());
		}
		traverse(root.left, res, list);
		traverse(root.right, res, list);
		list.remove(list.size() - 1);
	}
	
	
	public static void main(String[] args) {
		BinaryTreePaths tree = new BinaryTreePaths();
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
		System.out.println(tree.binaryTreePaths(root));
	}

}
