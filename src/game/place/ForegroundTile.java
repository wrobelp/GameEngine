/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.place;

import collision.OpticProperties;
import collision.Rectangle;
import sprites.SpriteSheet;

/**
 *
 * @author Wojtek
 */
public class ForegroundTile extends Tile {

    private static int type;
    private boolean blockPart;

    public static ForegroundTile createOrdinaryShadowHeight(SpriteSheet spriteSheet, int size, int xSheet, int ySheet, int yStart) {
        return new ForegroundTile(spriteSheet, size, xSheet, ySheet, false, yStart, false);
    }

    public static ForegroundTile createOrdinary(SpriteSheet spriteSheet, int size, int xSheet, int ySheet) {
        return new ForegroundTile(spriteSheet, size, xSheet, ySheet, false, 0, false);
    }

    public static ForegroundTile createWallShadowHeight(SpriteSheet spriteSheet, int size, int xSheet, int ySheet, int yStart) {
        return new ForegroundTile(spriteSheet, size, xSheet, ySheet, true, yStart, false);
    }

    public static ForegroundTile createWall(SpriteSheet spriteSheet, int size, int xSheet, int ySheet) {
        return new ForegroundTile(spriteSheet, size, xSheet, ySheet, true, 0, false);
    }

    public static ForegroundTile createRoundOrdinaryShadowHeight(SpriteSheet spriteSheet, int size, int xSheet, int ySheet, int yStart) {
        return new ForegroundTile(spriteSheet, size, xSheet, ySheet, false, yStart, true);
    }

    public static ForegroundTile createRoundOrdinary(SpriteSheet spriteSheet, int size, int xSheet, int ySheet) {
        return new ForegroundTile(spriteSheet, size, xSheet, ySheet, false, 0, true);
    }

    public static ForegroundTile createRoundWallShadowHeight(SpriteSheet spriteSheet, int size, int xSheet, int ySheet, int yStart) {
        return new ForegroundTile(spriteSheet, size, xSheet, ySheet, true, yStart, true);
    }

    public static ForegroundTile createRoundWall(SpriteSheet spriteSheet, int size, int xSheet, int ySheet) {
        return new ForegroundTile(spriteSheet, size, xSheet, ySheet, true, 0, true);
    }

    protected ForegroundTile(SpriteSheet spriteSheet, int size, int xSheet, int ySheet, boolean wall, int yStart, boolean round) {
        super(spriteSheet, size, xSheet, ySheet);
        solid = wall;
        type = wall ? OpticProperties.FULL_SHADOW : OpticProperties.IN_SHADE_NO_SHADOW;
        setCollision(Rectangle.create(0, yStart, size, size, type, this));
        simpleLighting = !round;
    }

    //0  1 2 3       4    5      6          7
    //ft:x:y:texture:wall:yStart:TileXSheet:TileYSheet...
    public String saveToString(SpriteSheet s, int xBegin, int yBegin, int tile) {
        String txt = "ft:" + ((getX() - xBegin) / tile) + ":" + ((getY() - yBegin) / tile) + ":" + (spriteSheet.equals(s) ? "" : spriteSheet.getKey());
        txt += ":" + (solid ? "1" : "0") + ":" + (collision.getYStart() / tile);
        txt = tileStack.stream().map((p) -> ":" + p.getX() + ":" + p.getY()).reduce(txt, String::concat);
        return txt;
    }

    public String saveToStringAsTile(SpriteSheet s, int xBegin, int yBegin, int tile) {
        String txt = "t:" + ((getX() - xBegin) / tile) + ":" + ((getY() - yBegin) / tile) + ":" + (spriteSheet.equals(s) ? "" : spriteSheet.getKey());
        txt = tileStack.stream().map((p) -> ":" + p.getX() + ":" + p.getY()).reduce(txt, String::concat);
        return txt;
    }

    public void setBlockPart(boolean blockPart) {
        this.blockPart = blockPart;
    }

    public boolean isInBlock() {
        return blockPart;
    }

    public boolean isWall() {
        return collision.isGiveShadow();
    }
}
