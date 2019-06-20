
public class ConstructBinaryTreefromInorderandPostorderTraversal {

	public TreeNode buildTree(int[] inorder, int[] postorder) {
		if(inorder == null || postorder == null || inorder.length != postorder.length) return null;
		return build(inorder, postorder, 0, inorder.length - 1, postorder.length - 1);
	}
	private TreeNode build(int[] inorder, int[] postorder, int left, int right, int location){
		if(left > right) return null;
		int mid = left;
		int curr = postorder[location];
		while(mid <= right && inorder[mid++] != curr);
		mid--;
		int len =  right - mid;
		TreeNode node = new TreeNode(curr);
		node.left = build(inorder, postorder, left, mid - 1, location - len - 1);
		node.right = build(inorder, postorder, mid + 1, right, location - 1);
		return node;
	}
	
	public static void main(String[] args) {
		ConstructBinaryTreefromInorderandPostorderTraversal construct = new ConstructBinaryTreefromInorderandPostorderTraversal();
		construct.buildTree(new int[]{}, new int[]{});
		construct.buildTree(new int[]{9,3,15,20,7}, new int[]{9,15,7,20,3});
		construct.buildTree(new int[]{3,9}, new int[]{3,9});
		construct.buildTree(new int[]{3}, new int[]{3});
	}

}
