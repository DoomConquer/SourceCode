
public class KthSmallestElementinaBST {

	public int kthSmallest(TreeNode root, int k) {
		return kth(root, k);
	}
	int num = 0;
	private Integer kth(TreeNode root, int k){
		if(root == null) return null;
		Integer left = kth(root.left, k);
		if(left != null) return left;
		num++;
		if(num == k) return root.val;
		Integer right = kth(root.right, k);
		if(right != null) return right;
		return null;
	}
	
	public static void main(String[] args) {
		KthSmallestElementinaBST kth = new KthSmallestElementinaBST();
		TreeNode root = new TreeNode(4);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(3);
		TreeNode node3 = new TreeNode(6);
		root.left = node1;
		node1.right = node2;
		root.right = node3;
		System.out.println(kth.kthSmallest(root, 3));
		kth.num = 0;
		System.out.println(kth.kthSmallest(root, 1));
		kth.num = 0;
		System.out.println(kth.kthSmallest(root, 2));
		kth.num = 0;
		System.out.println(kth.kthSmallest(root, 4));
	}

}
