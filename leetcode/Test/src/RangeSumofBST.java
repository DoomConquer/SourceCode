public class RangeSumofBST {

    public int rangeSumBST(TreeNode root, int L, int R) {
    	if(root == null) return 0;
    	if(root.val > R)
    		return rangeSumBST(root.left, L, R);
    	else if(root.val < L)
    		return rangeSumBST(root.right, L, R);
    	else
    		return root.val + rangeSumBST(root.left, L, R) + rangeSumBST(root.right, L, R);
    }
    
	public static void main(String[] args) {
		RangeSumofBST RangeSumofBST = new RangeSumofBST();
		TreeNode root = new TreeNode(10);
		TreeNode node1 = new TreeNode(5);
		TreeNode node2 = new TreeNode(15);
		TreeNode node3 = new TreeNode(3);
		TreeNode node4 = new TreeNode(7);
		TreeNode node5 = new TreeNode(18);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		node2.right = node5;
		System.out.println(RangeSumofBST.rangeSumBST(root, 7, 15));
		System.out.println(RangeSumofBST.rangeSumBST(root, 3, 7));
	}

}
