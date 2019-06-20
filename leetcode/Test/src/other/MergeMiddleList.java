package other;

public class MergeMiddleList {

	public Node reOrderLink(Node head){
		Node res = new Node(0);
		Node currNode = res;
		Node pNode = head, qNode = findMiddleNode(head);
		Node midNode = qNode;
		while(pNode != midNode){
			currNode.next = pNode;
			currNode = currNode.next;
			pNode = pNode.next;
			if(qNode != null){
				currNode.next = qNode;
				currNode = currNode.next;
				qNode = qNode.next;
			}
		}
		currNode.next = null;
		if(qNode != null) currNode.next = qNode;
		return res.next;
	}
	private Node findMiddleNode(Node head){
		Node slow = head, fast = head;
		while(fast != null && fast.next != null){
			slow = slow.next;
			fast = fast.next.next;
		}
		return slow;
	}
	
	public static void main(String[] args) {
		// 1->2->3->4->5
		Node head = new Node(1);
		Node node1 = new Node(2);
		Node node2 = new Node(3);
		Node node3 = new Node(4);
		Node node4 = new Node(5);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		node3.next = node4;
		MergeMiddleList mergeMid = new MergeMiddleList();
		Node res = mergeMid.reOrderLink(head);
		while(res != null){ System.out.print(res.val + " "); res = res.next; }
		System.out.println();
		
		// 1->2->3->4
		head = new Node(1);
		node1 = new Node(2);
		node2 = new Node(3);
		node3 = new Node(4);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		res = mergeMid.reOrderLink(head);
		while(res != null){ System.out.print(res.val + " "); res = res.next; }
		System.out.println();
		
		// 1
		head = new Node(1);
		res = mergeMid.reOrderLink(head);
		while(res != null){ System.out.print(res.val + " "); res = res.next; }
		System.out.println();
		
		// 1->2
		head = new Node(1);
		node1 = new Node(2);
		head.next = node1;
		res = mergeMid.reOrderLink(head);
		while(res != null){ System.out.print(res.val + " "); res = res.next; }
		System.out.println();
		
		// null
		res = mergeMid.reOrderLink(null);
		while(res != null){ System.out.print(res.val + " "); res = res.next; }
		System.out.println();
	}

}
class Node{
	int val;
	Node next;
	public Node(int val){
		this.val = val;
	}
}
