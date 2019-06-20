import java.util.LinkedList;
import java.util.Queue;

public class FindBottomLeftTreeValue {

	public int findBottomLeftValue(TreeNode root) {
		if(root == null) return 0;
		Queue<TreeNode> queue = new LinkedList<>();
		queue.offer(root);
		int num = 0;
		while(!queue.isEmpty()){
			int size = queue.size();
			for(int i = 0; i < size; i++){
				TreeNode node = queue.poll();
				if(i == 0) num = node.val;
				if(node.left != null) queue.offer(node.left);
				if(node.right != null) queue.offer(node.right);
			}
		}
		return num;
	}
	
	public static void main(String[] args) {
		FindBottomLeftTreeValue tree = new FindBottomLeftTreeValue();
		TreeNode root = new TreeNode(10);
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
		System.out.println(tree.findBottomLeftValue(root));
	}

}
