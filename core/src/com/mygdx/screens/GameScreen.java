package com.mygdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.mygdx.helpers.BodyHelper;
import com.mygdx.helpers.Constants;
import com.mygdx.helpers.ContactType;
import com.mygdx.helpers.FancyFontHelper;
import com.mygdx.helpers.GameContactListener;
import com.mygdx.helpers.ScreenType;
import com.mygdx.objects.Ball;
import com.mygdx.objects.Player;
import com.mygdx.objects.PlayerAI;
import com.mygdx.objects.Wall;
import com.mygdx.pong.PongGame;
import com.mygdx.objects.Obstacle;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the main game screen
 */
public class GameScreen extends ScreenAdapter {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	// World is needed within the Box2D library
	private World world;
	// Uncomment for debugging
	//private Box2DDebugRenderer debugRenderer;
	private BitmapFont font;

	//objects
	private Player player;
	private PlayerAI ai;
	private List<Ball> balls;
	private Wall upper;
	private Wall lower;
	private Obstacle obstacle;

	private int hitCounter = 0;
	private boolean shouldSpawnNewBall = false;

	public GameScreen(OrthographicCamera camera) {
		Box2D.init();

		this.camera = camera;
		this.camera.position.set(new Vector3(PongGame.getInstance().getWindowWidth()/2,
				PongGame.getInstance().getWindowHeight()/2, 0));
		this.batch = new SpriteBatch();
		this.world = new World(new Vector2(0, 0), false);

		// Uncomment for debugging
		//this.debugRenderer = new Box2DDebugRenderer();

		this.world.setContactListener(new GameContactListener(this));

		// Body creation: paddles are kinematic bodies
		Body playerBody = BodyHelper.createRectangularBody(16,
				PongGame.getInstance().getWindowHeight() / 2,
				Constants.AI_PADDLE_WIDTH,
				Constants.AI_PADDLE_HEIGHT,
				BodyType.KinematicBody,
				1f,
				getWorld(),
				ContactType.AI);
		this.player = new Player(16, PongGame.getInstance().getWindowHeight() / 2, playerBody);

		// Create the obstacle at the center of the screen
Body obstacleBody = BodyHelper.createRectangularBody(
    PongGame.getInstance().getWindowWidth() / 2, 
    PongGame.getInstance().getWindowHeight() / 2, 
    Constants.AI_PADDLE_WIDTH, 
    Constants.AI_PADDLE_HEIGHT, 
    BodyType.KinematicBody, 
    1f, 
    getWorld(), 
    ContactType.AI
);
this.obstacle = new Obstacle(
    PongGame.getInstance().getWindowWidth() / 2, 
    PongGame.getInstance().getWindowHeight() / 2, 
    obstacleBody, 
    this // Pass the GameScreen instance
	);



		Body aiBody = BodyHelper.createRectangularBody(PongGame.getInstance().getWindowWidth() - 16,
				PongGame.getInstance().getWindowHeight() / 2,
				Constants.AI_PADDLE_WIDTH,
				Constants.AI_PADDLE_HEIGHT,
				BodyType.KinematicBody,
				1f,
				getWorld(),
				ContactType.AI);
		this.ai = new PlayerAI(PongGame.getInstance().getWindowWidth() - 16,
				PongGame.getInstance().getWindowHeight() / 2,
				aiBody,
				this);

		this.balls = new ArrayList<>();
		this.balls.add(new Ball(this));

		this.upper = new Wall(PongGame.getInstance().getWindowHeight() - (Constants.UPPER_WALL_SIZE/2),
				Constants.UPPER_WALL_SIZE,
				this);

		this.lower = new Wall((Constants.LOWER_WALL_SIZE/2),
				Constants.LOWER_WALL_SIZE,
				this);

		this.font = FancyFontHelper.getInstance().getFont(Color.WHITE, 40);
	}

	public void registerHit() {
		hitCounter++;
		if (hitCounter % 10 == 0) {  // how many hits it takes to spawn another ball
			shouldSpawnNewBall = true;
		}
	}

	public void update() {
		this.world.step(1/60f, 6, 2);

		// updating all required bodies
		this.camera.update();
		this.player.update();
		this.ai.update();

		// Spawn new ball if needed
		if (shouldSpawnNewBall) {
			balls.add(new Ball(this));
			shouldSpawnNewBall = false;
		}

		// Create a copy of the list for safe iteration
		List<Ball> ballsCopy = new ArrayList<>(balls);

		// Update all balls
		for (Ball ball : ballsCopy) {
			ball.update();

			// The ball is reset to the centre if it goes our of the screen
			// player or AI scores are updated accordingly
			if(ball.getX() + 3*ball.getRadius() < 0) {
				this.ai.updateScrore();
				ball.reset();
			}

			if(ball.getX() - 3*ball.getRadius() > PongGame.getInstance().getWindowWidth()) {
				this.player.updateScrore();
				ball.reset();
			}
		}

		this.batch.setProjectionMatrix(this.camera.combined);

		// Reset button in case the ball gets stuck horizontally
		if(Gdx.input.isKeyPressed(Input.Keys.R)) {
			for (Ball ball : balls) {
				ball.reset();
			}
		}

		// To return to the menu screen
		if(Gdx.input.isKeyPressed(Input.Keys.M))
			PongGame.getInstance().changeScreen(this, ScreenType.MENU);

		// Checks if the game is over, and transitions to the end game screen
		if(hasPlayerWon() || hasAIWon())
			PongGame.getInstance().changeScreen(this, ScreenType.END_GAME, getWinnerMessage());
	}

	@Override
	public void render(float delta) {
		update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Simple rendering of bodies and scores
		this.batch.begin();

		this.player.render(batch);
		this.ai.render(batch);

		for (Ball ball : balls) {
			ball.render(batch);
		}

		this.upper.render(batch);
		this.lower.render(batch);

		this.font.draw(batch,
				""+this.player.getScore(),
				50,
				PongGame.getInstance().getWindowHeight() - Constants.UPPER_WALL_SIZE/2 + 15);

		this.font.draw(batch,
				""+this.ai.getScore(),
				PongGame.getInstance().getWindowWidth() - 50,
				PongGame.getInstance().getWindowHeight() - Constants.UPPER_WALL_SIZE/2 + 15);

		this.batch.end();

		// Uncomment for debugging
		//this.debugRenderer.render(world, camera.combined.scl(Constants.PPM));
	}

	// Getter methods for world and ball
	public World getWorld() {
		return world;
	}

	public Ball getBall() {
		return balls.get(0);
	}

	// Private auxiliary method to check winning conditions and create the end game message
	private boolean hasPlayerWon() {
		return this.player.getScore() == Constants.END_SCORE;
	}

	private boolean hasAIWon() {
		return this.ai.getScore() == Constants.END_SCORE;
	}

	private String getWinnerMessage() {
		return hasPlayerWon() ?
				"You Won!\n  " + this.player.getScore() + " - " + this.ai.getScore() :
				"You Lost!\n  " + this.player.getScore() + " - " + this.ai.getScore();
	}
}