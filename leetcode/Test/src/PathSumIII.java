import java.util.HashMap;
import java.util.Map;

public class PathSumIII {

	public int pathSum(TreeNode root, int sum) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(0, 1);
		return path(root, 0, sum, map);
	}
	private int path(TreeNode root, int curr, int sum, Map<Integer, Integer> map){
		if(root == null) return 0;
		curr += root.val;
		int preSum = map.getOrDefault(curr - sum, 0);
		map.put(curr, map.getOrDefault(curr, 0) + 1);
		int count = preSum + path(root.left, curr, sum, map) + path(root.right, curr, sum, map);
		map.put(curr, map.get(curr) - 1);
		return count;
	}
	
	public static void main(String[] args) {
		PathSumIII path = new PathSumIII();
		TreeNode root = new TreeNode(10);
		TreeNode node1 = new TreeNode(5);
		TreeNode node2 = new TreeNode(-3);
		TreeNode node3 = new TreeNode(3);
		TreeNode node4 = new TreeNode(2);
		TreeNode node5 = new TreeNode(11);
		TreeNode node6 = new TreeNode(3);
		TreeNode node7 = new TreeNode(-2);
		TreeNode node8 = new TreeNode(1);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		node2.right = node5;
		node3.left = node6;
		node3.right = node7;
		node4.right = node8;
		System.out.println(path.pathSum(root, 8));
		
		root = new TreeNode(5);
		node1 = new TreeNode(4);
		node2 = new TreeNode(8);
		node3 = new TreeNode(11);
		node4 = new TreeNode(13);
		node5 = new TreeNode(4);
		node6 = new TreeNode(7);
		node7 = new TreeNode(2);
		node8 = new TreeNode(5);
		TreeNode node9 = new TreeNode(1);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node2.left = node4;
		node2.right = node5;
		node3.left = node6;
		node3.right = node7;
		node5.left = node8;
		node5.right = node9;
		System.out.println(path.pathSum(root, 22));
	}

}
