
public class HouseRobberIII {
	private int[] robHouse(TreeNode root) {
        if(root == null)
        	return new int[2];
        int rob = 0;
        int notRob = 0;
        
        int[] left = robHouse(root.left);
        int[] right = robHouse(root.right);
        rob = max(left[0] + right[0] + root.val, left[1] + right[0], left[0] + right[1], left[1] + right[1]);
        notRob = max(left[1] + right[1], left[1] + right[0], left[0] + right[1], left[0] + right[0]);
        return new int[]{notRob, rob};
    }
	private int max(int a, int b, int c, int d){
		return Math.max(a, Math.max(b, Math.max(c, d)));
	}
	public int rob(TreeNode root){
		int[] res = robHouse(root);
		return Math.max(res[0], res[1]);
	}

	public static void main(String[] args) {
//		TreeNode root = new TreeNode(3);
//		TreeNode node1 = new TreeNode(2);
//		TreeNode node2 = new TreeNode(3);
//		TreeNode node3 = new TreeNode(3);
//		TreeNode node4 = new TreeNode(1);
//		root.left = node1;
//		root.right = node2;
//		node1.right = node3;
//		node2.right = node4;
		
//		TreeNode root = new TreeNode(3);
//		TreeNode node1 = new TreeNode(4);
//		TreeNode node2 = new TreeNode(5);
//		TreeNode node3 = new TreeNode(1);
//		TreeNode node4 = new TreeNode(3);
//		TreeNode node5 = new TreeNode(1);
//		root.left = node1;
//		root.right = node2;
//		node1.left = node3;
//		node1.right = node4;
//		node2.right = node5;
		
		TreeNode root = new TreeNode(4);
		TreeNode node1 = new TreeNode(1);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(3);
		root.left = node1;
		node1.left = node2;
		node2.left = node3;
		
		HouseRobberIII robber = new HouseRobberIII();
		System.out.println(robber.rob(root));
	}

}
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int x) { val = x; }
	}
