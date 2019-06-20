import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BinaryTreeLevelOrderTraversal {

	 public List<List<Integer>> levelOrder(TreeNode root) {
		 List<List<Integer>> res = new ArrayList<List<Integer>>();
			Queue<TreeNode> queue = new LinkedList<TreeNode>();
			Queue<TreeNode> slave = new LinkedList<TreeNode>();
			if(root == null) return res;
			queue.add(root);
			while(!queue.isEmpty()){
				List<Integer> list = new ArrayList<Integer>();
				while(!queue.isEmpty()){
					TreeNode node = queue.poll();
					list.add(node.val);
					if(node.left != null) slave.add(node.left);
					if(node.right != null) slave.add(node.right);
				}
				Queue<TreeNode> temp = queue;
				queue = slave;
				slave = temp;
				res.add(list);
			}
			return res;
	 }
	 
	public static void main(String[] args) {
		BinaryTreeLevelOrderTraversal levelOrder = new BinaryTreeLevelOrderTraversal();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(3);
		TreeNode node3 = new TreeNode(7);
		root.left = node1;
		root.right = node2;
		node2.right = node3;
		System.out.println(levelOrder.levelOrder(root));
	}

}
