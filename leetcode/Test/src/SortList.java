
public class SortList {

	public ListNode sortList(ListNode head) {
		if(head == null || head.next == null) return head;
		ListNode p = head, q = head, pre = head;
		while(q != null && q.next != null){
			pre = p;
			p = p.next;
			q = q.next.next;
		}
		pre.next = null;
		ListNode l1 = sortList(head);
		ListNode l2 = sortList(p);
		return merge(l1, l2);
	}
	private ListNode merge(ListNode l1, ListNode l2){
		ListNode node = new ListNode(0);
		ListNode p = l1, q = l2, r = node;
		while(p != null && q != null){
			if(p.val > q.val){
				r.next = q;
				r = r.next;
				q = q.next;
			}else{
				r.next = p;
				r = r.next;
				p = p.next;
			}
		}
		if(p != null) r.next = p;
		if(q != null) r.next = q;
		return node.next;
	}
	
	public static void main(String[] args) {
		SortList sort = new SortList();
		ListNode head = new ListNode(0);
		ListNode l1 = new ListNode(-7);
		ListNode l2 = new ListNode(1);
		ListNode l3 = new ListNode(4);
		ListNode l4 = new ListNode(3);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		l3.next = l4;
		ListNode res = sort.sortList(head);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
