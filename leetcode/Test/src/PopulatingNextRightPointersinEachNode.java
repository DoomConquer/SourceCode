
public class PopulatingNextRightPointersinEachNode {

	public void connect(TreeLinkNode root) {
		connect(root, null, true);
	}
	private void connect(TreeLinkNode root, TreeLinkNode parent, boolean isLeft){
		if(root == null) return;
		if(parent != null){
			if(isLeft){
				root.next = parent.right;
			}else{
				root.next = parent.next != null ? parent.next.left : null;
			}
		}else{
			root.next = null;
		}
		
		connect(root.left, root, true);
		connect(root.right, root, false);
	}
	
	public static void main(String[] args) {
		PopulatingNextRightPointersinEachNode tree = new PopulatingNextRightPointersinEachNode();
		TreeLinkNode root = new TreeLinkNode(0);
		TreeLinkNode node1 = new TreeLinkNode(1);
		TreeLinkNode node2 = new TreeLinkNode(2);
		TreeLinkNode node3 = new TreeLinkNode(3);
		TreeLinkNode node4 = new TreeLinkNode(4);
		TreeLinkNode node5 = new TreeLinkNode(5);
		TreeLinkNode node6 = new TreeLinkNode(6);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		node2.left = node5;
		node2.right = node6;
		tree.connect(root);
	}

}

class TreeLinkNode {
	 int val;
	 TreeLinkNode left, right, next;
	 TreeLinkNode(int x) { val = x; }
}