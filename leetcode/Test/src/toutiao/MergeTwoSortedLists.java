package toutiao;

public class MergeTwoSortedLists {

    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode head = new ListNode(0);
        ListNode currNode = head;
        while(l1 != null && l2 != null){
        	if(l1.val > l2.val){
        		currNode.next = l2;
        		currNode = currNode.next;
				l2 = l2.next;
        	}else{
        		currNode.next = l1;
        		currNode = currNode.next;
        		l1 = l1.next;
        	}
        }
        if(l1 != null) currNode.next = l1;
        if(l2 != null) currNode.next = l2;
        return head.next;
    }
    
	public static void main(String[] args) {
		MergeTwoSortedLists merge = new MergeTwoSortedLists();
		ListNode l1 = new ListNode(1);
		ListNode l11 = new ListNode(1);
		ListNode l12 = new ListNode(1);
		ListNode l13 = new ListNode(2);
		l1.next = l11;
		l11.next = l12;
		l12.next = l13;
		ListNode l2 = new ListNode(0);
		ListNode l21 = new ListNode(1);
		ListNode l22 = new ListNode(1);
		ListNode l23 = new ListNode(1);
		l2.next = l21;
		l21.next = l22;
		l22.next = l23;
		ListNode node = merge.mergeTwoLists(l1, l2);
		while(node != null){
			System.out.print(node.val + "  ");
			node = node.next;
		}
	}

}
