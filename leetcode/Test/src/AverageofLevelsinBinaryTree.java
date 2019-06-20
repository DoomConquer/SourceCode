import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AverageofLevelsinBinaryTree {

	public List<Double> averageOfLevels(TreeNode root) {
		List<Double> res = new ArrayList<Double>();
		Queue<TreeNode> queue = new LinkedList<TreeNode>();
		Queue<TreeNode> slave = new LinkedList<TreeNode>();
		if(root == null) return res;
		queue.add(root);
		while(!queue.isEmpty()){
			double sum = 0;
			int count = queue.size();
			while(!queue.isEmpty()){
				TreeNode node = queue.poll();
				sum += node.val;
				if(node.left != null) slave.add(node.left);
				if(node.right != null) slave.add(node.right);
			}
			res.add(sum / count);
			Queue<TreeNode> temp = queue;
			queue = slave;
			slave = temp;
		}
		return res;
	}
	
	public static void main(String[] args) {
		AverageofLevelsinBinaryTree tree = new AverageofLevelsinBinaryTree();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(3);
		TreeNode node3 = new TreeNode(7);
		root.left = node1;
		root.right = node2;
		node2.right = node3;
		
		System.out.println(tree.averageOfLevels(root));
	}

}
