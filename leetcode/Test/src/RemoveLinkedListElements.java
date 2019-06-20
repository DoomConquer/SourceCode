
public class RemoveLinkedListElements {

	public ListNode removeElements(ListNode head, int val) {
		ListNode node = new ListNode(0);
		ListNode q = node;
		while(head != null){
			if(head.val == val){
				head = head.next;
				continue;
			}
			q.next = head;
			q = q.next;
			head = head.next;
			q.next = null;
		}
		return node.next;
	}
	
	public static void main(String[] args) {
		RemoveLinkedListElements remove = new RemoveLinkedListElements();
		ListNode head = new ListNode(4);
		ListNode l1 = new ListNode(9);
		ListNode l2 = new ListNode(4);
		head.next = l1;
		l1.next = l2;
		ListNode res = remove.removeElements(head, 4);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
