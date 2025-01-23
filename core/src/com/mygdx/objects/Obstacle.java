// package com.mygdx.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.mygdx.helpers.BodyHelper;
import com.mygdx.helpers.Constants;
import com.mygdx.helpers.ContactType;
import com.mygdx.screens.GameScreen;
import com.badlogic.gdx.Gdx;
import com.mygdx.PongGame;

public class Obstacle extends PlayerPaddle {
    private float speed = 100f;
    private float direction = 1f;
    private GameScreen gameScreen; // Reference to GameScreen

    public Obstacle(float x, float y, Body body, GameScreen gameScreen) {
        super(x, y, body);
        this.gameScreen = gameScreen;

        Pixmap pixmap = new Pixmap(Constants.PLAYER_PADDLE_WIDTH, Constants.PLAYER_PADDLE_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        this.texture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void update() {
        // Use gameScreen to get window height
        y += direction * speed * Gdx.graphics.getDeltaTime();

        if (y + Constants.PLAYER_PADDLE_HEIGHT > gameScreen.getWindowHeight()) {
            y = gameScreen.getWindowHeight() - Constants.PLAYER_PADDLE_HEIGHT;
            direction = -1f;
        } else if (y < 0) {
            y = 0;
            direction = 1f;
        }

        body.setTransform(x / Constants.PPM, y / Constants.PPM, body.getAngle());

        x = body.getPosition().x * Constants.PPM - (Constants.PLAYER_PADDLE_WIDTH / 2);
        y = body.getPosition().y * Constants.PPM - (Constants.PLAYER_PADDLE_HEIGHT / 2);
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        spriteBatch.draw(texture, x, y, Constants.PLAYER_PADDLE_WIDTH, Constants.PLAYER_PADDLE_HEIGHT);
    }
}
