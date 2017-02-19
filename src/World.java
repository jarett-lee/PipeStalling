import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

public class World 
{
	private static List<Player> playerList;
	private List<Block> blockList;
	private List<Mine> mineList;
	private int numPlayers;
	private double width;
	private double height;
	private long startCountdown = 0;
	private int go;
	private boolean fell;

	private GameSettings settings;

	public World(double width, double height, GameSettings game, int[] playerCharacters)
	{
		this.width = width;
		this.height = height;
		playerList = new ArrayList<Player>();
		blockList = new ArrayList<Block>();	
		mineList = new ArrayList<Mine>();
		settings = game;
		numPlayers = playerCharacters.length;
		assignTypes(playerCharacters);
		if(numPlayers == 2)
			spawnTwo();
		if(numPlayers == 3)
			spawnThree();
		if(numPlayers == 4)
			spawnFour();
	}

	public void assignTypes(int[] playerCharacters)
	{
		for(int i = 0; i < numPlayers; i++)
		{
			if(playerCharacters[i] == 0)
				playerList.add(new Loadstar(0, 0));
			if(playerCharacters[i] == 1)
				playerList.add(new Bulbastore(0, 0));
			if(playerCharacters[i] == 2)
				playerList.add(new Jumpernaut(0, 0));
			if(playerCharacters[i] == 3)
				playerList.add(new MadAdder(0, 0));
		}
	}

	public void spawnTwo()
	{
		playerList.get(0).setXPos(width/10 - 50);
		playerList.get(0).setYPos(height/10);
		playerList.get(1).setXPos(width*9/10 - 50);
		playerList.get(1).setYPos(height/10);	
	}

	public void spawnThree()
	{
		spawnTwo();
		playerList.get(2).setXPos(width/2 - 50);
		playerList.get(2).setYPos(height*4/5);
		blockList.add(new Block(width/2 - 50, height*4/5, 100, 10));				
	}

	public void spawnFour()
	{
		spawnTwo();
		playerList.get(2).setXPos(width/10 - 50);
		playerList.get(2).setYPos(height*4/5);
		blockList.add(new Block(width/10 - 50, height*4/5, 100, 10));
		playerList.get(3).setXPos(width*9/10 - 50);
		playerList.get(3).setYPos(height*4/5);
		blockList.add(new Block(width*9/10 - 50, height*4/5, 100, 10));	
	}

	public double getWidth(){
		return width;
	}

	public double getHeight(){
		return height;
	}

	public void update(double delta)
	{

		if(startCountdown == 0)
		{
			startCountdown = System.currentTimeMillis();
		}

		if(go < 3)
		{
			long countdown = System.currentTimeMillis() - startCountdown;
			if(countdown >= 3000)
			{
				go = 3;
			}
			else if(countdown >= 2000)
			{
				go = 2;
			}
			else if(countdown >= 1000)
			{
				go = 1;
				return;
			}
			else
			{
				go = 0;
				return;
			}
		}

		registerKeys();

		for(Player player : playerList) {
			//block collisions, y-axis
			fell = true;
			if(player.getCharacter() == CharacterType.STORE)
			{
				((Bulbastore)player).updateTimer();
			}
			player.updateYMotion();
			if(player.getYPos() >= height) {
				player.setYPos(0 - player.getHeight());
			} else if(player.getYPos() <= 0 - player.getHeight()) {
				player.setYPos(height);
			} else {
				for(Block block : blockList) {
					if(isColliding(player, block)){
						fell = false;
						if(player.getYVel() > 0) {
							player.setYPos(block.getYPos() - player.getHeight());
						} else {
							player.setYPos(block.getYPos()+block.getHeight());
							player.land();

						}
					}
				}
				//player collisions, y-axis
				for(Player otherPlayer : playerList) {
					if(otherPlayer.getCharacter() != player.getCharacter()) 
					{
						if(isColliding(player, otherPlayer)){
							fell = false;
							if(player.getYVel() > 0) {
								player.setYPos(otherPlayer.getYPos() - player.getHeight());
								if(player.isSlamming())
								{
									player.damage(5, otherPlayer);
								}
							} else {
								player.setYPos(otherPlayer.getYPos()+otherPlayer.getHeight());
								player.setYVel(0);
								if(player.getCharacter() == CharacterType.JUMP && player.usingSpecial)
								{
									player.damage(5, otherPlayer);
								}
								player.land();
							}
						}
					}
				}
			}

			//block collisions, x-axis
			if(fell)
			{
				if(player.getCharacter() == CharacterType.JUMP)
				{
					if(!player.usingSpecial)
					{
						player.fall();
					}
				}
				else
				{
					player.fall();
				}
			}
			player.updateXMotion();
			if(player.getXPos() >= width) {
				player.setXPos(0-player.getWidth());
			} else if (player.getXPos() <= 0 - player.getWidth()) {
				player.setXPos(width);
			} else {
				for(Block block : blockList) {
					if(isColliding(player, block)) {
						if(player.getXVel() > 0) {
							player.setXPos(block.getXPos()-player.getWidth());
							player.setXVel(0);
						} else {
							player.setXPos(block.getXPos()+block.getWidth());
							player.setXVel(0);
						}
					}
				}
				//player collisions, x-axis
				for(Player otherPlayer : playerList) {
					if(otherPlayer.getCharacter() != player.getCharacter())
						if(isColliding(player, otherPlayer)) {
							if(player.getXVel() > 0) {
								player.setXPos(otherPlayer.getXPos()-player.getWidth());
								player.setXVel(0);
							} else {
								player.setXPos(otherPlayer.getXPos()+otherPlayer.getWidth());
								player.setXVel(0);
							}
						}
				}
			}
		}

	}

	public void render(double delta, Vector3d[] players, Vector3d blocks, Vector3d text)
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 1920, 1080, 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		if(go < 3)
		{
			float ticks = (System.currentTimeMillis() - this.startCountdown) % 1000f / 1000f;
			float size = .5f + ticks;
			float alpha = 1 - Math.abs(ticks - .5f) * 2;
			GL11.glColor4d(text.x, text.y, text.z, alpha);
			settings.getFont().bind();
			if(go == 0)
			{
				settings.getFont().draw("READY?", 1920 / 2 - settings.getFont().getWidth("READY?", size) / 2, 540 + settings.getFont().getSize() * size / 2, 0, size);
			}
			else if(go == 1)
			{
				settings.getFont().draw("SET", 1920 / 2 - settings.getFont().getWidth("SET", size) / 2, 540 + settings.getFont().getSize() * size / 2, 0, size);
			}
			else
			{
				settings.getFont().draw("GO!", 1920 / 2 - settings.getFont().getWidth("GO!", size) / 2, 540 + settings.getFont().getSize() * size / 2, 0, size);

			}
			settings.getFont().unbind();
		}

		int windowWidth = settings.getWindowWidth();
		int windowHeight = settings.getWindowHeight();
		GL11.glViewport(0, 0, windowWidth, windowHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		double ymax = .01 * Math.tan( 60 * Math.PI / 360.0 );
		double ymin = -ymax;
		double xmin = ymin * windowWidth / windowHeight;
		double xmax = ymax * windowWidth / windowHeight;
		GL11.glFrustum( xmin, xmax, ymin, ymax, .01, 2000);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glColor4f(1, 1, 1, 1);
		for(int i = 0; i < players.length; i++)
		{
			playerList.get(i).render(delta, players[i]);
		}

		for(Block b : blockList)
		{
			b.render(delta, blocks);
		}
	}

	public List<Player> getPlayers(){
		return playerList;
	}
	public List<Block> getBlocks(){
		return blockList;
	}
	/*
	 * Adds a block to the world. Returns false if already in world.
	 */
	public boolean addBlock(Block block){
		if(blockList.contains(block)){
			return false;
		}
		blockList.add(block);
		return true;
	}

	public boolean addMine(Mine mine){
		if(mineList.contains(mine)){
			return false;
		}
		mineList.add(mine);
		return true;
	}
	/*
	 * Adds a player to the world. Returns false if already in world.
	 */
	public boolean addPlayer(Player player){
		if(playerList.contains(player)){
			return false;
		}
		playerList.add(player);
		return true;
	}

	public void updateVelocityAcceleration(Player player){
		double yVel = player.getYVel();
		double xVel = player.getXVel();
		double yAcc = player.getYAcc();
		double xAcc = player.getXAcc();
		double oldX = player.getXPos();
		double oldY = player.getYPos();

		player.setYVel(yVel + yAcc);
		player.setXVel(xVel + xAcc);
		player.setXPos(oldX + xVel);
		player.setYPos(oldY + yVel);
	}

	public boolean isColliding(Block p1, Block p2) {
		return (p1.getXPos() < p2.getXPos() + p2.getWidth() &&
				p1.getXPos() + p1.getWidth() > p2.getXPos() &&
				p1.getYPos() < p2.getYPos() + p2.getHeight() &&
				p1.getHeight() + p1.getYPos() > p2.getYPos());
	}

	public boolean isLowerBoundColliding(Block p1, Block p2) {
		return !(p1.getHeight() + p1.getYPos() > p2.getYPos());
	}

	public void registerKeys()
	{
		for(int i = 0; i < playerList.size(); i++)
		{
			if(settings.getPlayerJump(i).isPressed())
			{
				playerList.get(i).jump();
			}

			if(settings.getPlayerSmash(i).isPressed())
			{
				playerList.get(i).slam();
			}
			
			if(settings.getPlayerRight(i).isDoublePressed())
			{
				playerList.get(i).setXVel(22);
				playerList.get(i).facingLeft = false;
			}
			
			else if(settings.getPlayerRight(i).isPressed())
			{
				playerList.get(i).setXVel(15);
				playerList.get(i).facingLeft = false;
			}
			
			else if(settings.getPlayerLeft(i).isDoublePressed())
			{
				playerList.get(i).setXVel(-22);
				playerList.get(i).facingLeft = true;
			}		
			
			else if(settings.getPlayerLeft(i).isPressed())
			{
				playerList.get(i).setXVel(-15);
				playerList.get(i).facingLeft = true;
			}

			else
			{
				playerList.get(i).setXAcc(-playerList.get(i).getXVel());
			}

			if(settings.getPlayerBeam(i).wasQuickPressed())
			{
				Beam shot = new Beam(playerList.get(i), 7);
				shot.shootBeam();
			}
			
			if(settings.getPlayerSuper(i).wasQuickPressed())
			{
				if(playerList.get(i).getCharacter() == CharacterType.ADD){
				((MadAdder)playerList.get(i)).special();
				}
			}

			if(settings.getPlayerSuper(i).isPressed())
			{
				if(playerList.get(i).getCharacter() == CharacterType.LOAD) {
					((Loadstar)playerList.get(i)).special();
				}
				if(playerList.get(i).getCharacter() == CharacterType.JUMP){
					((Jumpernaut)playerList.get(i)).special();
				}
				if(playerList.get(i).getCharacter() == CharacterType.STORE) {
					((Bulbastore)(playerList.get(i))).startTimer();
					((Bulbastore)playerList.get(i)).special();
				}
			} 
		}

	}
}
