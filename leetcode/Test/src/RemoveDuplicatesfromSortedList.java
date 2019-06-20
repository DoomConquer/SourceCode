
public class RemoveDuplicatesfromSortedList {

	public ListNode deleteDuplicates(ListNode head) {
		ListNode node = new ListNode(0);
		ListNode p = head, q = node;
		while(p != null){
			if(p.next != null && p.val == p.next.val){
				ListNode temp = p;
				p = p.next;
				temp.next = null;
				continue;
			}
			q.next = p;
			q = q.next;
			p = p.next;
		}
		return node.next;
	}
	
	public static void main(String[] args) {
		RemoveDuplicatesfromSortedList remove = new RemoveDuplicatesfromSortedList();
		ListNode head = new ListNode(0);
		ListNode l1 = new ListNode(1);
		ListNode l2 = new ListNode(3);
		ListNode l3 = new ListNode(3);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		ListNode node = remove.deleteDuplicates(head);
		while(node != null){
			System.out.print(node.val + "  ");
			node = node.next;
		}
	}

}
