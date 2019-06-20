
public class ConstructStringfromBinaryTree {
	
	public String tree2str(TreeNode t) {
		StringBuilder sb = new StringBuilder();
		traverse(t, sb);
       return sb.toString();
    }
	
	private void traverse(TreeNode t, StringBuilder sb){
		if(t == null) return;
		sb.append(t.val);
		if(t.left == null && t.right == null){
			return;
		}
		sb.append("(");
		traverse(t.left, sb);
		sb.append(")");
		if(t.right != null){
			sb.append("(");
			traverse(t.right, sb);
			sb.append(")");
		}
	}
	
	public static void main(String[] args) {
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(3);
		TreeNode node3 = new TreeNode(4);
		root.left = node1;
		root.right = node2;
		node1.right = node3;
		ConstructStringfromBinaryTree constructStringfromBinaryTree = new ConstructStringfromBinaryTree();
		System.out.println(constructStringfromBinaryTree.tree2str(root));
		
	}
}
