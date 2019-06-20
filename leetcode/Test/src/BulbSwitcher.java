
public class BulbSwitcher {

	public int bulbSwitch(int n) {
		return (int)Math.sqrt(n);
	}
	
	public static void main(String[] args) {
		BulbSwitcher bulb = new BulbSwitcher();
		System.out.println(bulb.bulbSwitch(100));
		System.out.println(bulb.bulbSwitch(3));
	}

}
