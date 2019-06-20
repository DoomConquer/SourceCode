import java.util.ArrayList;
import java.util.List;

public class FindLargestValueinEachTreeRow {

	public List<Integer> largestValues(TreeNode root) {
		List<Integer> res = new ArrayList<>();
		traverse(root, res, 1);
		return res;
	}
	private void traverse(TreeNode root, List<Integer> res, int height) {
		if (root == null) return;
		if (res.size() < height) {
			res.add(root.val);
		} else {
			if(res.get(height - 1) < root.val) res.set(height - 1, root.val);
		}
		traverse(root.left, res, height + 1);
		traverse(root.right, res, height + 1);
	}

	public static void main(String[] args) {
		FindLargestValueinEachTreeRow find = new FindLargestValueinEachTreeRow();
		TreeNode root = new TreeNode(0);
		TreeNode node1 = new TreeNode(1);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(3);
		TreeNode node5 = new TreeNode(5);
		TreeNode node6 = new TreeNode(6);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node2.left = node5;
		node2.right = node6;
		System.out.println(find.largestValues(root));
	}

}
