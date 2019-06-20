import java.util.Comparator;
import java.util.PriorityQueue;

public class MergekSortedLists {

	public ListNode mergeKLists(ListNode[] lists) {
		if(lists == null ||lists.length == 0) return null;
		ListNode res = new ListNode(0);
		ListNode curr = res;
		PriorityQueue<ListNode> heap = new PriorityQueue<>(new Comparator<ListNode>(){
			@Override
			public int compare(ListNode o1, ListNode o2) {
				return o1.val - o2.val;
			}
		});
		for(ListNode node : lists)
			if(node != null)
				heap.add(node);
		while(!heap.isEmpty()){
			ListNode node = heap.poll();
			if(node.next != null) heap.add(node.next);
			curr.next = node;
			curr = curr.next;
		}
		return res.next;
	}
	
	public static void main(String[] args) {
		MergekSortedLists merge = new MergekSortedLists();
		ListNode[] lists = new ListNode[3];
		ListNode list11 = new ListNode(1);
		ListNode list12 = new ListNode(2);
		ListNode list13 = new ListNode(3);
		list11.next = list12;
		list12.next = list13;
		ListNode list21 = new ListNode(5);
		ListNode list22 = new ListNode(7);
		ListNode list23 = new ListNode(8);
		list21.next = list22;
		list22.next = list23;
		ListNode list31 = new ListNode(1);
		ListNode list32 = new ListNode(2);
		ListNode list33 = new ListNode(13);
		list31.next = list32;
		list32.next = list33;
		lists[0] = list11;
		lists[1] = list21;
		lists[2] = list31;
		ListNode node = merge.mergeKLists(lists);
		while(node != null){
			System.out.print(node.val + "  ");
			node = node.next;
		}
	}

}
