import java.util.ArrayList;
import java.util.List;

public class PathSumII {

	public List<List<Integer>> pathSum(TreeNode root, int sum) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		path(root, sum, res, new ArrayList<>());
		return res;
	}
	private void path(TreeNode root, int sum, List<List<Integer>> res, List<Integer> temp){
		if(root == null) return;
		temp.add(root.val);
		if(root.left == null && root.right == null){
			if(sum - root.val == 0){
				List<Integer> list = new ArrayList<>();
				for(int num : temp)
					list.add(num);
				res.add(list);
			}
		}
		path(root.left, sum - root.val, res, temp);
		path(root.right, sum - root.val, res, temp);
		if(temp.size() > 0) temp.remove(temp.size() - 1);
	}
	
	public static void main(String[] args) {
		PathSumII path = new PathSumII();
		TreeNode root = new TreeNode(5);
		TreeNode node1 = new TreeNode(4);
		TreeNode node2 = new TreeNode(8);
		TreeNode node3 = new TreeNode(11);
		TreeNode node4 = new TreeNode(13);
		TreeNode node5 = new TreeNode(4);
		TreeNode node6 = new TreeNode(7);
		TreeNode node7 = new TreeNode(2);
		TreeNode node8 = new TreeNode(5);
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
		List<List<Integer>> res = path.pathSum(root, 22);
		for(List<Integer> list : res)
			System.out.println(list);
	}

}
