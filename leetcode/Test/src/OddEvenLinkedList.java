
public class OddEvenLinkedList {

	public ListNode oddEvenList(ListNode head) {
		if(head == null || head.next == null) return head;
		ListNode odd = head, even = head.next, p = odd, q = even, pNext = odd, qNext = even;
		while(pNext != null && qNext != null){
			p.next = pNext;
			p = p.next;
			pNext = qNext.next;
			
			q.next = qNext;
			q = q.next;
			q.next = null;
			if(pNext != null)
				qNext = pNext.next;
		}
		if(pNext != null){
			p.next = pNext;
			p = p.next;
		}
		p.next = even;
		return odd;
	}
	
	public static void main(String[] args) {
		OddEvenLinkedList oddEven = new OddEvenLinkedList();
		ListNode head = new ListNode(0);
		ListNode l1 = new ListNode(-7);
		ListNode l2 = new ListNode(1);
		ListNode l3 = new ListNode(4);
		ListNode l4 = new ListNode(3);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		l3.next = l4;
		ListNode res = oddEven.oddEvenList(head);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
