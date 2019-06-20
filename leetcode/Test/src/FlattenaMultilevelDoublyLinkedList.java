import java.util.Stack;

public class FlattenaMultilevelDoublyLinkedList {

    public Node flatten(Node head) {
        Stack<Node> stack = new Stack<Node>();
        Node currNode = head, pre = null;
        while(currNode != null || !stack.isEmpty()){
        	if(currNode == null){
        		currNode = stack.pop();
        		pre.next = currNode;
        		currNode.prev = pre;
        	}
        	if(currNode.child != null){
        		if(currNode.next != null)
        			stack.push(currNode.next);
        		currNode.next = currNode.child;
        		currNode.child.prev = currNode;
        		currNode.child = null;
        	}
        	pre = currNode;
        	currNode = currNode.next;
        }
        return head;
    }
    
	public static void main(String[] args) {
		FlattenaMultilevelDoublyLinkedList flattenaMultilevelDoublyLinkedList = new FlattenaMultilevelDoublyLinkedList();
		Node head = new Node();
		Node node1 = new Node();
		Node node2 = new Node();
		Node node3 = new Node();
		Node node4 = new Node();
		Node node5 = new Node();
		Node node6 = new Node();
		Node node7 = new Node();
		
		head.val = 1;
		head.prev = null;
		head.next = node1;
		
		node1.val = 2;
		node1.prev = head;
		node1.next = node2;
		node1.child = node3;
		
		node2.val = 3;
		node2.prev = node1;
		node2.next = null;
		
		node3.val = 4;
		node3.prev = null;
		node3.next = node4;
		
		node4.val = 5;
		node4.prev = node3;
		node4.next = null;
		node4.child = node5;
		
		node5.val = 6;
		node5.next = node6;
		
		node6.val = 7;
		node6.prev = node5;
		node6.next = node7;
		
		node7.val = 8;
		node7.prev = node6;
		node7.next = null;
		Node res = flattenaMultilevelDoublyLinkedList.flatten(head);
		while(res != null){ System.out.print(res.val + " "); res = res.next; }
	}

}
class Node {
    public int val;
    public Node prev;
    public Node next;
    public Node child;

    public Node() {}

    public Node(int _val, Node _prev, Node _next, Node _child) {
        val = _val;
        prev = _prev;
        next = _next;
        child = _child;
    }
};
