
public class ReverseLinkedListII {

	public ListNode reverseBetween(ListNode head, int m, int n) {
		ListNode node = new ListNode(0);
		ListNode p = head, q = node;
		n = n - m + 1;
		while(p != null && --m > 0){
			q.next = p;
			q = q.next;
			p = p.next;
		}
		q.next = null;
		int i = n;
		ListNode last = null;
		while(p != null && n-- > 0){
			if(i - 1 == n) last = p;
			ListNode temp = q.next;
			q.next = p;
			p = p.next;
			q.next.next = temp;
		}
		q = last;
		while(p != null){
			q.next = p;
			q = q.next;
			p = p.next;
		}
		return node.next;
	}
	
	public static void main(String[] args) {
		ReverseLinkedListII reverse = new ReverseLinkedListII();
		ListNode head = new ListNode(0);
		ListNode l1 = new ListNode(1);
		ListNode l2 = new ListNode(3);
		ListNode l3 = new ListNode(4);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		ListNode res = reverse.reverseBetween(head, 2, 3);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
