package toutiao;

public class SortList {

    public ListNode sortList(ListNode head) {
        ListNode mid = findMid(head);
        if(mid == head) return head;
        return mergeList(sortList(head), sortList(mid));
    }
    private ListNode findMid(ListNode head){
    	ListNode slow = head, fast = head, pre = null;
    	while(fast != null && fast.next != null){
    		pre = slow;
    		slow = slow.next;
    		fast = fast.next.next;
    	}
    	if(pre != null) pre.next = null;
    	return slow;
    }
    private ListNode mergeList(ListNode node1, ListNode node2){
    	ListNode head = new ListNode(0);
    	ListNode currNode = head;
    	while(node1 != null && node2 != null){
    		if(node1.val > node2.val){
    			currNode.next = node2;
    			currNode = currNode.next;
    			node2 = node2.next;
    		}else{
    			currNode.next = node1;
    			currNode = currNode.next;
    			node1 = node1.next;
    		}
    	}
    	if(node1 != null) currNode.next = node1;
    	if(node2 != null) currNode.next = node2;
    	return head.next;
    }
    
	public static void main(String[] args) {
		SortList sortList = new SortList();
		ListNode head = new ListNode(0);
		ListNode l1 = new ListNode(-7);
		ListNode l2 = new ListNode(1);
		ListNode l3 = new ListNode(4);
		ListNode l4 = new ListNode(3);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		l3.next = l4;
		ListNode res = sortList.sortList(head);
		while(res != null){ System.out.print(res.val + " "); res = res.next; }
	}

}
