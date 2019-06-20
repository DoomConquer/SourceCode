import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NaryTreePreorderTraversal {

	class Node {
	    public int val;
	    public List<Node> children;

	    public Node() {}

	    public Node(int _val,List<Node> _children) {
	        val = _val;
	        children = _children;
	    }
	};
    public List<Integer> preorder(Node root) {
    	List<Integer> list = new ArrayList<>();
    	preorder(root, list);
    	return list;
    }
    private void preorder(Node root, List<Integer> list){
    	if(root == null) return;
    	list.add(root.val);
    	if(root.children != null){
    		for(Node node : root.children) preorder(node, list);
    	}
    }
    
	public static void main(String[] args) {
		NaryTreePreorderTraversal naryTreePreorderTraversal = new NaryTreePreorderTraversal();
		Node node5 = naryTreePreorderTraversal.new Node(6, null);
		Node node4 = naryTreePreorderTraversal.new Node(5, null);
		Node node3 = naryTreePreorderTraversal.new Node(4, null);
		Node node2 = naryTreePreorderTraversal.new Node(2, null);
		Node node1 = naryTreePreorderTraversal.new Node(3, Arrays.asList(node4, node5));
		Node root = naryTreePreorderTraversal.new Node(1, Arrays.asList(node1, node2, node3));
		System.out.println(naryTreePreorderTraversal.preorder(root));
	}

}
