
public class SumRoottoLeafNumbers {

	public int sumNumbers(TreeNode root) {
		sumTree(root, new StringBuilder());
		return sum;
	}
	int sum = 0;
	private void sumTree(TreeNode root, StringBuilder sb){
		if(root == null) return;
		sb.append(root.val);
		if(root.left == null && root.right == null) sum += Integer.valueOf(sb.toString());
		sumTree(root.left, sb);
		sumTree(root.right, sb);
		sb.deleteCharAt(sb.length() - 1);
	}
	
	public static void main(String[] args) {
		SumRoottoLeafNumbers tree = new SumRoottoLeafNumbers();
		TreeNode root = new TreeNode(0);
		TreeNode node1 = new TreeNode(4);
		TreeNode node2 = new TreeNode(8);
		TreeNode node3 = new TreeNode(9);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		System.out.println(tree.sumNumbers(root));
	}

}
