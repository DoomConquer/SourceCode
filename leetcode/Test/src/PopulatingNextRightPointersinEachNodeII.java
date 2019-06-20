import java.util.LinkedList;
import java.util.Queue;

public class PopulatingNextRightPointersinEachNodeII {

	public void connect(TreeLinkNode root) {
		if(root == null) return;
		Queue<TreeLinkNode> queue = new LinkedList<>();
		queue.offer(root);
		while(!queue.isEmpty()){
			int size = queue.size();
			TreeLinkNode pre = null;
			for(int i = 0; i < size; i++){
				TreeLinkNode node = queue.poll();
				if(pre != null) pre.next = node;
				pre = node;
				if(node.left != null) queue.offer(node.left);
				if(node.right != null) queue.offer(node.right);
			}
			if(pre != null) pre.next = null;
		}
	}
	
	public static void main(String[] args) {
		PopulatingNextRightPointersinEachNodeII tree = new PopulatingNextRightPointersinEachNodeII();
		TreeLinkNode root = new TreeLinkNode(0);
		TreeLinkNode node1 = new TreeLinkNode(1);
		TreeLinkNode node2 = new TreeLinkNode(2);
		TreeLinkNode node3 = new TreeLinkNode(3);
		TreeLinkNode node5 = new TreeLinkNode(5);
		TreeLinkNode node6 = new TreeLinkNode(6);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node2.left = node5;
		node2.right = node6;
		tree.connect(root);
	}

}
