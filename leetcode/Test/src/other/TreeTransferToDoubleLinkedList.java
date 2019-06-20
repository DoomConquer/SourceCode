package other;

public class TreeTransferToDoubleLinkedList {

	private TreeNode preNode, headNode;
	public TreeNode transfer(TreeNode root){
		trans(root);
		return headNode;
	}
	private void trans(TreeNode root){
		if(root == null) return;
		trans(root.lchild);
		if(preNode != null){
			preNode.rchild = root;
			root.lchild = preNode;
		}else headNode = root;
		preNode = root;
		trans(root.rchild);
	}
	
	public static void main(String[] args) {
		TreeTransferToDoubleLinkedList treeTransferToDoubleLinkedList = new TreeTransferToDoubleLinkedList();
		TreeNode root = new TreeNode(0);
		TreeNode node1 = new TreeNode(1);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(3);
		root.lchild = node1;
		root.rchild = node2;
		node2.lchild = node3;
		TreeNode res = treeTransferToDoubleLinkedList.transfer(root);
		while(res != null){ System.out.println(res.val); res = res.rchild; }
	}
	
}
class TreeNode{
		TreeNode lchild, rchild;
		int val;
		public TreeNode(int val){
			this.val = val;
		}
	}