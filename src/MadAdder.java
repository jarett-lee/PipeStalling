
public class MadAdder extends Player {

	public MadAdder(double x, double y) {
		super(x, y);
		characterType = CharacterType.ADD;
	}
	
	
	
	public void special() {
		usingSpecial = true;
		
	}
	
	public void render(double delta) {
		
	}
}
