
public class RemoveNthNodeFromEndofList {

	public ListNode removeNthFromEnd(ListNode head, int n) {
		if(head == null || n == 0) return head;
		ListNode p = head, q = head, pre = null;
		while(q != null){
			q = q.next;
			if(n <= 0){ pre = p; p = p.next; }
			n--;
		}
		if(pre != null){
			if(p != null)
				pre.next = p.next;
			else
				pre.next = null;
		}else{
			head = head.next;
		}
		return head;
	}
	
	public static void main(String[] args) {
		RemoveNthNodeFromEndofList remove = new RemoveNthNodeFromEndofList();
		ListNode head = new ListNode(1);
		ListNode node1 = new ListNode(2);
		ListNode node2 = new ListNode(3);
		ListNode node3 = new ListNode(4);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		ListNode node = remove.removeNthFromEnd(head, 4);
		while(node != null){
			System.out.print(node.val + "  ");
			node = node.next;
		}
	}

}
