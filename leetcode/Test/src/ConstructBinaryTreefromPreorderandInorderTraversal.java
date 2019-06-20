
public class ConstructBinaryTreefromPreorderandInorderTraversal {

	public TreeNode buildTree(int[] preorder, int[] inorder) {
		if(preorder == null || inorder == null || preorder.length != inorder.length) return null;
		return build(preorder, inorder, 0, preorder.length - 1, 0);
	}
	private TreeNode build(int[] preorder, int[] inorder, int left, int right, int location){
		if(left > right) return null;
		int curr = preorder[location];
		int mid = left;
		while(mid <= right && curr != inorder[mid++]);
		mid--;
		int len = mid - left;
		TreeNode node = new TreeNode(curr);
		node.left = build(preorder, inorder, left, mid - 1, location + 1);
		node.right = build(preorder, inorder, mid + 1, right, location + len + 1);
		return node;
	}
	
	public static void main(String[] args) {
		ConstructBinaryTreefromPreorderandInorderTraversal construct = new ConstructBinaryTreefromPreorderandInorderTraversal();
		construct.buildTree(new int[]{3,9,20,15,7}, new int[]{9,3,15,20,7});
		construct.buildTree(new int[]{3,9}, new int[]{9,3});
		construct.buildTree(new int[]{3}, new int[]{3});
		
	}

}
