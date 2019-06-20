import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShoppingOffers {

	public int shoppingOffers(List<Integer> price, List<List<Integer>> special, List<Integer> needs) {
		return shopping(price, special, needs, 0);
	}
	private int shopping(List<Integer> price, List<List<Integer>> special, List<Integer> needs, int layer){
		int minPrice = 0;
		for(int i = 0; i < needs.size(); i++)
			minPrice += needs.get(i) * price.get(i);
		
		for(int i = layer; i < special.size(); i++){
			List<Integer> leftNeeds = new ArrayList<>();
			List<Integer> offer = special.get(i);
			for(int j = 0; j < offer.size() - 1; j++){
				if(offer.get(j) > needs.get(j)){
					leftNeeds = null;
					break;
				}
				leftNeeds.add(needs.get(j) - offer.get(j));
			}
			if(leftNeeds == null) continue;
			minPrice = Math.min(minPrice, offer.get(offer.size() - 1) + shopping(price, special, leftNeeds, i));
		}
		return minPrice;
	}
	
	public static void main(String[] args) {
		ShoppingOffers offer = new ShoppingOffers();
		List<Integer> price = Arrays.asList(new Integer[]{2,3,4});
		List<List<Integer>> special = Arrays.asList(Arrays.asList(new Integer[]{1,1,0,4}), Arrays.asList(new Integer[]{2,2,1,9}));
		List<Integer> needs = Arrays.asList(new Integer[]{1,2,1});
		System.out.println(offer.shoppingOffers(price, special, needs));
		price = Arrays.asList(new Integer[]{2,5});
		special = Arrays.asList(Arrays.asList(new Integer[]{3,0,5}), Arrays.asList(new Integer[]{1,2,10}));
		needs = Arrays.asList(new Integer[]{3,2});
		System.out.println(offer.shoppingOffers(price, special, needs));
	}

}
