
public class FriendsOfAppropriateAges {

	public int numFriendRequests(int[] ages) {
		int[] age = new int[121];
		int[] sum = new int[121];
		for(int i = 0; i < ages.length; i++) age[ages[i]]++;
		for(int i = 1; i < 121; i++) sum[i] = sum[i - 1] + age[i];
		int total = 0;
		for(int i = 15; i < 121; i++){
			if(age[i] == 0) continue;
			int num = sum[i] - sum[i / 2 + 7];
			total += (num - 1) * age[i];
		}
		return total;
	}
	
	public static void main(String[] args) {
		FriendsOfAppropriateAges friend = new FriendsOfAppropriateAges();
		System.out.println(friend.numFriendRequests(new int[]{20,30,100,110,120}));
		System.out.println(friend.numFriendRequests(new int[]{16,17,18}));
	}

}
