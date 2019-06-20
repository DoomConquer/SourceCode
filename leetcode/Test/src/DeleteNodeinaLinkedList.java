
public class DeleteNodeinaLinkedList {

	public void deleteNode(ListNode node) {
		if(node == null) return;
		ListNode pre = node;
		while(node.next != null){
			node.val = node.next.val;
			pre = node;
			node = node.next;
		}
		pre.next = null;
	}
	
	public static void main(String[] args) {
		DeleteNodeinaLinkedList delete = new DeleteNodeinaLinkedList();
		ListNode node = new ListNode(4);
		ListNode l1 = new ListNode(9);
		ListNode l2 = new ListNode(4);
		node.next = l1;
		l1.next = l2;
		delete.deleteNode(l1);
	}

}
