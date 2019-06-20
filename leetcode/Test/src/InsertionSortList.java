
public class InsertionSortList {

	public ListNode insertionSortList(ListNode head) {
		ListNode node = new ListNode(0);
		while(head != null){
			ListNode p = node.next, pre = node;
			while(p != null){
				if(p.val > head.val) break;
				pre = p;
				p = p.next;
			}
			ListNode temp = pre.next;
			pre.next = head;
			head = head.next;
			pre.next.next = temp;
		}
		return node.next;
	}
	
	public static void main(String[] args) {
		InsertionSortList insert = new InsertionSortList();
		ListNode head = new ListNode(1);
		ListNode l2 = new ListNode(3);
		ListNode l3 = new ListNode(-1);
		head.next = l2;
		l2.next = l3;
		ListNode res = insert.insertionSortList(head);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
