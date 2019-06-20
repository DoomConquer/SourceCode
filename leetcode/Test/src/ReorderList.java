
public class ReorderList {

	public void reorderList(ListNode head) {
		ListNode p = head, q = head, pre = null;
		while(p != null && q != null){
			pre = p;
			p = p.next;
			if(q.next != null)
				q = q.next.next;
			else break;
		}
		if(pre != null)
			pre.next = null;
		ListNode head2 = new ListNode(0);
		q = head2;
		while(p != null){
			ListNode temp = q.next;
			q.next = p;
			p = p.next;
			q.next.next = temp;
		}
		p = head;
		q = head2.next;
		while(q != null){
			ListNode temp = p.next;
			p.next = q;
			q = q.next;
			p.next.next = temp;
			p = p.next.next;
		}
	}
	
	public static void main(String[] args) {
		ReorderList reorder = new ReorderList();
		ListNode head = new ListNode(0);
		ListNode l1 = new ListNode(1);
		ListNode l2 = new ListNode(3);
		ListNode l3 = new ListNode(4);
		ListNode l4 = new ListNode(5);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		l3.next = l4;
		reorder.reorderList(head);
		while(head != null){
			System.out.print(head.val + "  ");
			head = head.next;
		}
	}

}
