
public class SplitLinkedListinParts {

	public ListNode[] splitListToParts(ListNode root, int k) {
		int len = 0;
		ListNode p = root;
		while(p != null){
			len++;
			p = p.next;
		}
		ListNode[] res = new ListNode[k];
		int base = len / k;
		int overflow = len % k;
		p = root;
		int i = 0;
		while(i < k){
			res[i++] = p;
			int j = base;
			ListNode pre = p;
			while(j-- > 0) {
				pre = p;
				p = p.next;
			}
			if(overflow-- > 0){
				pre = p;
				p = p.next;
			}
			if(pre != null)
				pre.next = null;
		}
		return res;
	}
	
	public static void main(String[] args) {
		SplitLinkedListinParts split = new SplitLinkedListinParts();
		ListNode root = new ListNode(1);
		ListNode l2 = new ListNode(2);
		ListNode l3 = new ListNode(3);
		root.next = l2;
		l2.next = l3;
		split.splitListToParts(root, 2);
	}

}
