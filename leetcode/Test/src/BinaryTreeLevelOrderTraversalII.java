import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class BinaryTreeLevelOrderTraversalII {

	public List<List<Integer>> levelOrderBottom(TreeNode root) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		Stack<List<Integer>> stack = new Stack<List<Integer>>();
		Queue<TreeNode> queue = new LinkedList<TreeNode>();
		Queue<TreeNode> temp = new LinkedList<TreeNode>();
		if(root != null) queue.add(root);
		while(!queue.isEmpty()){
			List<Integer> level = new ArrayList<Integer>();
			for(TreeNode node : queue){
				level.add(node.val);
			}
			stack.push(level);
			while(!queue.isEmpty()){
				TreeNode curr = queue.poll();
				if(curr.left != null) temp.add(curr.left);
				if(curr.right != null) temp.add(curr.right);
			}
			if(!temp.isEmpty()){
				queue.addAll(temp);
				temp.clear();
			}
		}
		while(!stack.isEmpty())
			res.add(stack.pop());
		return res;
	}
	
	public static void main(String[] args) {
		BinaryTreeLevelOrderTraversalII levelOrder = new BinaryTreeLevelOrderTraversalII();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(4);
		TreeNode node3 = new TreeNode(7);
		root.left = node1;
		root.right = node2;
		node2.right = node3;
		
		System.out.println(levelOrder.levelOrderBottom(root));
	}

}
