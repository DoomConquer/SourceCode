import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BinaryTreeZigzagLevelOrderTraversal {

	public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
		Stack<TreeNode> stack = new Stack<TreeNode>();
		Stack<TreeNode> temp = new Stack<TreeNode>();
		List<List<Integer>> res = new ArrayList<>();
		if(root == null) return res;
		stack.push(root);
		int n = 0;
		while(!stack.isEmpty()){
			List<Integer> list = new ArrayList<Integer>();
			n++;
			while(!stack.isEmpty()){
				TreeNode node = stack.pop();
				list.add(node.val);
				if(n % 2 == 0){
					if(node.right != null) temp.push(node.right);
					if(node.left != null) temp.push(node.left);
				}else{
					if(node.left != null) temp.push(node.left);
					if(node.right != null) temp.push(node.right);
				}
			}
			res.add(list);
			Stack<TreeNode> s = stack;
			stack = temp;
			temp = s;
		}
		return res;
	}
	
	public static void main(String[] args) {
		BinaryTreeZigzagLevelOrderTraversal tree = new BinaryTreeZigzagLevelOrderTraversal();
		TreeNode root = new TreeNode(0);
		TreeNode node1 = new TreeNode(1);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(3);
		TreeNode node4 = new TreeNode(4);
		TreeNode node5 = new TreeNode(5);
		TreeNode node6 = new TreeNode(6);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		node2.left = node5;
		node2.right = node6;
		System.out.println(tree.zigzagLevelOrder(root));
	}

}
