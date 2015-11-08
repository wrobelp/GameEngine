package game.gameobject.interactive.collision;

import engine.utilities.Drawer;
import engine.utilities.Methods;
import game.gameobject.GameObject;
import game.gameobject.entities.Player;
import game.gameobject.interactive.InteractiveResponse;

import static game.gameobject.GameObject.*;
import game.place.Place;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslatef;
import org.newdawn.slick.Color;

/**
 * @author przemek
 */
public class CurveInteractiveCollision extends InteractiveCollision {

	private final int radius, activationAngle;

	public CurveInteractiveCollision(int fromBottom, int height, int shift, int radius, int activationAngle) {
		super(fromBottom, height, shift);
		this.radius = radius;
		this.activationAngle = activationAngle;
	}

	@Override
	public void updatePosition(GameObject owner) {
		int x = owner.getX();
		int y = owner.getY();
		switch (owner.getDirection8Way()) {
			case RIGHT:
				x += (owner.getCollision().getWidth() / 2 + shift);
				break;
			case UP:
				y -= (Methods.ONE_BY_SQRT_ROOT_OF_2 * (owner.getCollision().getWidth() / 2 + shift));
				break;
			case LEFT:
				x -= (owner.getCollision().getWidth() / 2 + shift);
				break;
			case DOWN:
				y += (Methods.ONE_BY_SQRT_ROOT_OF_2 * (owner.getCollision().getWidth() / 2 + shift));
				break;
			case UP_RIGHT:
				x += (Methods.ONE_BY_SQRT_ROOT_OF_2 * (owner.getCollision().getWidth() / 2 + shift));
				y -= owner.getCollision().getWidth() / 4 + shift / 2;
				break;
			case UP_LEFT:
				x -= (Methods.ONE_BY_SQRT_ROOT_OF_2 * (owner.getCollision().getWidth() / 2 + shift));
				y -= owner.getCollision().getWidth() / 4 + shift / 2;
				break;
			case DOWN_LEFT:
				x -= (Methods.ONE_BY_SQRT_ROOT_OF_2 * (owner.getCollision().getWidth() / 2 + shift));
				y += owner.getCollision().getWidth() / 4 + shift / 2;
				break;
			case DOWN_RIGHT:
				x += (Methods.ONE_BY_SQRT_ROOT_OF_2 * (owner.getCollision().getWidth() / 2 + shift));
				y += owner.getCollision().getWidth() / 4 + shift / 2;
				break;
		}
		position.set(x, y);
	}

	@Override
	protected InteractiveResponse collideImplementation(GameObject owner, GameObject object, byte attackType) {
		if (object != null && object.getCollision() != null) {
			int objectBottom = (int) object.getFloatHeight();
			int objectTop = objectBottom + object.getActualHeight();
			int bottom = (int) owner.getFloatHeight() + fromBottom;
			int top = bottom + height;
			if (objectTop > bottom && objectBottom < top) {
				int pixelsIn = circleToCircleDistance(position.getX(), position.getY(), object.getX(), object.getY(), radius, object.getCollisionWidth() / 2);
				if (pixelsIn > 0) {
					int direction = owner.getDirection8Way();
					double angle = Methods.pointAngleCounterClockwise(position.getX(), position.getY(), object.getX(), object.getY());
					if (direction == 0) {
						direction = 8;
					}
					if (angle == -0) {
						angle = 360;
					}
					double angleDifference = direction * 45 - angle;
					if (Math.abs(angleDifference) <= activationAngle || Math.abs(angleDifference - 360) <= activationAngle || Math.abs(angleDifference + 360)
							<= activationAngle) {
						response.setResponse(pixelsIn, shift + radius, (byte) (calculateInteractionDirection(object.getDirection8Way(),
								object.getCollision(), owner.getX(), owner.getY())), attackType, owner);
						return response;
					}
				}
			}
		}
		return InteractiveResponse.NO_RESPONSE;
	}

	@Override
	protected InteractiveResponse collideImplementation(GameObject owner, Player player, byte attackType) {
		if (player != null && player.isInGame()) {
			int playerBottom = (int) player.getFloatHeight();
			int playerTop = playerBottom + player.getActualHeight();
			int bottom = (int) owner.getFloatHeight() + fromBottom;
			int top = bottom + height;
			if (playerTop > bottom && playerBottom < top) {
				int pixelsIn = circleToCircleDistance(position.getX(), position.getY(), player.getX(), player.getY(), radius, player.getCollisionWidth() / 2);
				if (pixelsIn > 0) {
					int direction = owner.getDirection8Way();
					double angle = Methods.pointAngleCounterClockwise(position.getX(), position.getY(), player.getX(), player.getY());
					if (direction == 0) {
						direction = 8;
					}
					if (angle == -0) {
						angle = 360;
					}
					double angleDifference = direction * 45 - angle;
					if (Math.abs(angleDifference) <= activationAngle || Math.abs(angleDifference - 360) <= activationAngle || Math.abs(angleDifference + 360)
							<= activationAngle) {
						response.setResponse(pixelsIn, shift + radius, (byte) (calculateInteractionDirection(player.getDirection8Way(),
								player.getCollision(), owner.getX(), owner.getY())), attackType, owner);
						return response;
					}
				}
			}
		}
		return InteractiveResponse.NO_RESPONSE;
	}

	@Override
	public void render(GameObject owner, int xEffect, int yEffect) {
		Drawer.setColor(new Color(0.9f, 0.1f, 0.1f));
		glPushMatrix();
		glTranslatef(xEffect, yEffect, 0);
		glScaled(Place.getCurrentScale(), Place.getCurrentScale(), 1);
		int angle = 360 - (owner.getDirection8Way() * 45);
		Drawer.drawEllipseBow(position.getX(), position.getY(), radius, (int) (radius * Methods.ONE_BY_SQRT_ROOT_OF_2), (int) (radius * Methods.ONE_BY_SQRT_ROOT_OF_2), angle - activationAngle / 2, angle + activationAngle / 2, 16);
		Drawer.refreshColor();
		glPopMatrix();
	}
}