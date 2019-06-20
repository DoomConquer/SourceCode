public class CanPlaceFlowers {

    public boolean canPlaceFlowers(int[] flowerbed, int n) {
        if(n > flowerbed.length / 2 + 1) return false;
        if(flowerbed.length == 1 && flowerbed[0] != 1) return true;
        for(int i = 0; i < flowerbed.length && n > 0; i++){
        	if(i == 0){
        		if(flowerbed[0] == 0 && flowerbed[1] == 0){
        			flowerbed[0] = 1; n--;
        		}
        	}else if(i > 0 && i < flowerbed.length - 1){
        		if(flowerbed[i - 1] == 0 && flowerbed[i] == 0 && flowerbed[i + 1] == 0){
        			flowerbed[i] = 1; n--;
        		}
        	}else{
        		if(flowerbed[i - 1] == 0 && flowerbed[i] == 0){
        			flowerbed[i] = 1; n--;
        		}
        	}
        }
        if(n > 0) return false;
        return true;
    }
    
	public static void main(String[] args) {
		CanPlaceFlowers canPlaceFlowers = new CanPlaceFlowers();
		System.out.println(canPlaceFlowers.canPlaceFlowers(new int[]{1,0,0,0,1}, 1));
		System.out.println(canPlaceFlowers.canPlaceFlowers(new int[]{1,0,0,0,1}, 2));
	}

}
