public class LemonadeChange {

    public boolean lemonadeChange(int[] bills) {
        int d5 = 0, d10 = 0;
        for(int bill : bills){
        	switch(bill){
        	case 20:
        		if(d10 > 0 && d5 > 0){
        			d10--;
        			d5--;
        		}else if(d5 >= 3) d5 -= 3;
        		else return false;
        		break;
        	case 10:
        		d10++;
        		if(d5 > 0) d5--;
        		else return false;
        		break;
        	case 5:
        		d5++;
        		break;
        	}
        }
        return true;
    }
    
	public static void main(String[] args) {
		LemonadeChange lemonadeChange = new LemonadeChange();
		System.out.println(lemonadeChange.lemonadeChange(new int[]{5,5,5,10,20}));
		System.out.println(lemonadeChange.lemonadeChange(new int[]{5,5,10}));
		System.out.println(lemonadeChange.lemonadeChange(new int[]{10,10}));
		System.out.println(lemonadeChange.lemonadeChange(new int[]{5,5,10,10,20}));
	}

}
