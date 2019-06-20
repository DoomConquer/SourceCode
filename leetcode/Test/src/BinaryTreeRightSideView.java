import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BinaryTreeRightSideView {

	public List<Integer> rightSideView(TreeNode root) {
		Queue<TreeNode> queue = new LinkedList<>();
		Queue<TreeNode> temp = new LinkedList<>();
		List<Integer> res = new ArrayList<>();
		queue.add(root);
		while(!queue.isEmpty()){
			while(!queue.isEmpty()){
				TreeNode node = queue.poll();
				if(node != null){
					if(queue.size() == 0) res.add(node.val);
					if(node.left != null) temp.add(node.left);
					if(node.right != null) temp.add(node.right);
				}
			}
			Queue<TreeNode> q = queue;
			queue = temp;
			temp = q;
		}
		return res;
	}
	
	public static void main(String[] args) {
		BinaryTreeRightSideView tree = new BinaryTreeRightSideView();
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
		System.out.println(tree.rightSideView(root));
	}

}
