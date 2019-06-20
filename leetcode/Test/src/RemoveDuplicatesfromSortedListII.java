
public class RemoveDuplicatesfromSortedListII {

	public ListNode deleteDuplicates(ListNode head) {
		ListNode node = new ListNode(0);
		ListNode p = head, q = node, pNext;
		while(p != null){
			pNext = p;
			while(pNext.next != null && p.val == pNext.next.val) pNext = pNext.next;
			if(p == pNext){
				q.next = p;
				q = q.next;
				p = p.next;
				pNext.next = null;
			}else{
				p = pNext.next;
				pNext.next = null;
			}
		}
		return node.next;
	}
	
	public static void main(String[] args) {
		RemoveDuplicatesfromSortedListII remove = new RemoveDuplicatesfromSortedListII();
		ListNode head = new ListNode(1);
		ListNode l1 = new ListNode(1);
		ListNode l2 = new ListNode(2);
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
