package sprites.fbo;

import engine.matrices.MatrixMath;
import engine.utilities.Drawer;
import game.Settings;
import game.gameobject.entities.Player;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL30;
import sprites.Appearance;
import sprites.vbo.VertexBufferObject;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public abstract class FrameBufferObject implements Appearance {

    public static final int NATIVE = 0, ARB = 1, EXT = 2;
    private static final FrameBufferType REGULAR_NATIVE = new RegularNative();
    private static final FrameBufferType REGULAR_ARB = new RegularARB();
    private static final FrameBufferType REGULAR_EXT = new RegularEXT();
    private static final FrameBufferType MULTI_SAMPLE_NATIVE = new MultiSampleNative();
    private static final FrameBufferType MULTI_SAMPLE_ARB = new MultiSampleARB();
    private static final FrameBufferType MULTI_SAMPLE_EXT = new MultiSampleEXT();
    private static VertexBufferObject screenVBO;
    private static float lastScreenData[] = new float[10];
    private static float checkSum = 0;
    private static List<FrameBufferObject> instances = new ArrayList<>();
    final FrameBufferType type;
    final int height;
    final int width;
    final int texture;
    public boolean generated;
    float heightSlice;
    int frameBufferObject;
    int version;
    private VertexBufferObject vbo;

    FrameBufferObject(int width, int height, boolean multiSample) {
        this.width = width;
        this.height = height;
        texture = glGenTextures();
        version = Settings.supportedFrameBufferObjectVersion;
        if (multiSample && Settings.multiSampleSupported) {
            if (version == NATIVE) {
                type = MULTI_SAMPLE_NATIVE;
            } else if (version == ARB) {
                type = MULTI_SAMPLE_ARB;
            } else {
                type = MULTI_SAMPLE_EXT;
                version = EXT;
            }
        } else {
            if (version == NATIVE) {
                type = REGULAR_NATIVE;
            } else if (version == ARB) {
                type = REGULAR_ARB;
            } else {
                type = REGULAR_EXT;
                version = EXT;
            }
        }
        instances.add(this);
    }

    public static void cleanUp() {
        for (FrameBufferObject fbo : instances) {
            fbo.clear();
        }
        instances.clear();
    }

    public void setHeightSlice(float heightSlice) {
        this.heightSlice = heightSlice;
        if (vbo != null) {
            vbo.clear();
            vbo = null;
        }
    }

    protected void initializeBuffers() {
        if (heightSlice != 0) {
            float[] vertices = {
                    0, 0,
                    0, 0 + height,
                    0 + width, 0 + height,
                    0 + width, 0,
                    0, 0,
                    0, 0 + heightSlice,
                    0 + width, 0 + heightSlice,
                    0 + width, 0,
                    0, heightSlice,
                    0, 0 + height,
                    0 + width, 0 + height,
                    0 + width, heightSlice,
            };
            float[] textureCoordinates = {
                    0, 1f,                           //Całość
                    0, 0,
                    1f, 0,
                    1f, 1f,
                    0, heightSlice / height,         //Dół
                    0, 0,
                    1f, 0,
                    1f, heightSlice / height,
                    0, 1f,                          //Góra
                    0, heightSlice / height,
                    1f, heightSlice / height,
                    1f, 1f,
            };
            int[] indices = {0, 1, 3, 2, 4, 5, 7, 6, 8, 9, 11, 10};
            vbo = VertexBufferObject.create(vertices, textureCoordinates, indices);
        } else {
            float[] vertices = {
                    0, 0,
                    0, 0 + height,
                    0 + width, 0 + height,
                    0 + width, 0
            };
            float[] textureCoordinates = {
                    0, 1f,                           //Całość
                    0, 0,
                    1f, 0,
                    1f, 1f
            };
            int[] indices = {0, 1, 3, 2};
            vbo = VertexBufferObject.create(vertices, textureCoordinates, indices);
        }
        if (screenVBO == null) {
            float[] vertices = {
                    0, 0, 0, 0, 0, 0, 0, 0
            };
            float[] textureCoordinates = {
                    0, 0, 0, 0, 0, 0, 0, 0
            };
            int[] indices = {0, 1, 3, 2};
            screenVBO = VertexBufferObject.create(vertices, textureCoordinates, indices);
        }
    }

    public void clear() {
        for (int texture : getTextures()) {
            glDeleteTextures(texture);
        }
        for (int buffer : getBuffers()) {
            if (version == NATIVE) {
                GL30.glDeleteFramebuffers(buffer);
            } else if (version == ARB) {
                ARBFramebufferObject.glDeleteFramebuffers(frameBufferObject);
            } else {
                EXTFramebufferObject.glDeleteFramebuffersEXT(frameBufferObject);
            }
        }
    }

    public void delete() {
        instances.remove(this);
    }

    public abstract void activate();

    public abstract void deactivate();

    public void render() {
        render(0);
    }

    private void render(int type) {
        bindCheck();
        Drawer.spriteShader.start();
        Drawer.spriteShader.loadTextureShift(0, 0);
        Drawer.spriteShader.loadSizeModifier(ZERO_VECTOR);
        Drawer.spriteShader.loadTransformationMatrix(MatrixMath.STATIC_MATRIX);
        vbo.renderTextured(type * 4, 4);
        Drawer.spriteShader.stop();
    }

    public void renderShadow(float color) {
        renderShadow(0, color);
    }

    public void renderShadowBottom(float color) {
        renderShadow(1, color);
    }

    public void renderShadowTop(float color) {
        renderShadow(2, color);
    }

    public void renderShadow(int type, float color) {
        bindCheck();
        Drawer.shadowShader.start();
        vectorModifier.set(color, color, color, 1);
        Drawer.shadowShader.loadColourModifier(vectorModifier);
        Drawer.shadowShader.loadTextureShift(0, 0);
        Drawer.shadowShader.loadSizeModifier(ZERO_VECTOR);
        Drawer.shadowShader.loadTransformationMatrix(MatrixMath.STATIC_MATRIX);
        vbo.renderTextured(type * 4, 4);
        Drawer.shadowShader.stop();
    }

    public void renderBottom() {
        render(1);
    }

    public void renderTop() {
        render(2);
    }


    public void renderBottomPart(int partXStart, int partXEnd) {
        renderPart(1, partXStart, partXEnd);
    }


    public void renderTopPart(int partXStart, int partXEnd) {
        renderPart(2, partXStart, partXEnd);
    }


    public void renderShadowBottomPart(int partXStart, int partXEnd, float color) {
        renderShadowPart(1, partXStart, partXEnd, color);
    }

    public void renderShadowTopPart(int partXStart, int partXEnd, float color) {
        renderShadowPart(2, partXStart, partXEnd, color);
    }

    public void renderShadowPart(int type, int partXStart, int partXEnd, float color) {
        if (bindCheck()) {
            if (partXStart > partXEnd) {
                int temp = partXStart;
                partXStart = partXEnd;
                partXEnd = temp;
            }
            if (partXEnd > width) {
                partXEnd = width;
            }
            if (partXStart < 0) {
                partXStart = 0;
            }
            vectorModifier.set(color, color, color, 1f);
            Drawer.shadowShader.start();
            Drawer.shadowShader.loadTextureShift(0, 0);
            Drawer.shadowShader.loadColourModifier(vectorModifier);
            vectorModifier.set(partXStart, partXEnd - width, partXStart / (float) width, (partXEnd - width) / (float) width);
            Drawer.shadowShader.loadSizeModifier(vectorModifier);
            Drawer.shadowShader.loadTransformationMatrix(MatrixMath.STATIC_MATRIX);
            vbo.renderTextured(type * 4, 4);
            Drawer.shadowShader.stop();
        }
    }

    public void renderScreenPart(float displayWidth, float displayHeight, float xStart, float yStart, float xEnd, float yEnd, float xTStart, float yTStart,
                                 float xTEnd, float yTEnd) {
        if (areDifferent(displayWidth, displayHeight, xStart, yStart, xEnd, yEnd, xTStart, yTStart, xTEnd, yTEnd)) {
            float[] vertices = {
                    xStart * displayWidth, yStart * displayHeight,
                    xStart * displayWidth, yEnd * displayHeight,
                    xEnd * displayWidth, yEnd * displayHeight,
                    xEnd * displayWidth, yStart * displayHeight
            };
            float[] textureCoordinates = {
                    xTStart, yTEnd,
                    xTStart, yTStart,
                    xTEnd, yTStart,
                    xTEnd, yTEnd
            };
            screenVBO.updateVerticesAndTextureCoords(vertices, textureCoordinates);
        }
        bindCheck();
        Drawer.spriteShader.start();
        Drawer.spriteShader.loadTextureShift(0, 0);
        Drawer.spriteShader.loadSizeModifier(ZERO_VECTOR);
        Drawer.spriteShader.loadTransformationMatrix(MatrixMath.STATIC_MATRIX);
        screenVBO.renderTextured(0, 4);
        Drawer.spriteShader.stop();
    }


    private boolean areDifferent(float displayWidth, float displayHeight, float xStart, float yStart, float xEnd, float yEnd, float xTStart, float yTStart,
                                 float
                                         xTEnd, float yTEnd) {
        float curSum = displayWidth * displayHeight + xStart * yStart * xEnd * yEnd + xTStart * yTStart * xTEnd * yTEnd;
        if (curSum != checkSum) {
            checkSum = curSum;
            lastScreenData[0] = displayWidth;
            lastScreenData[1] = displayHeight;
            lastScreenData[2] = xStart;
            lastScreenData[3] = yStart;
            lastScreenData[4] = xEnd;
            lastScreenData[5] = yEnd;
            lastScreenData[6] = xTStart;
            lastScreenData[7] = yTStart;
            lastScreenData[8] = xTEnd;
            lastScreenData[9] = yTEnd;
            return true;
        }
        return false;
    }

    public int getTexture() {
        return texture;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public boolean bindCheck() {
        if (vbo == null) {
            initializeBuffers();
        }
        if (glGetInteger(GL_TEXTURE_BINDING_2D) != texture) {
            glBindTexture(GL_TEXTURE_2D, texture);
        }
        return true;
    }

    public void renderPart(int type, int partXStart, int partXEnd) {
        if (bindCheck()) {
            if (partXStart > partXEnd) {
                int temp = partXStart;
                partXStart = partXEnd;
                partXEnd = temp;
            }
            if (partXEnd > width) {
                partXEnd = width;
            }
            if (partXStart < 0) {
                partXEnd = 0;
            }
            vectorModifier.set(partXStart, partXEnd - width, partXStart / (float) width, (partXEnd - width) / (float) width);
            MatrixMath.resetMatrix(transformationMatrix);
            Drawer.spriteShader.start();
            Drawer.spriteShader.loadTextureShift(0, 0);
            Drawer.spriteShader.loadSizeModifier(vectorModifier);
            Drawer.spriteShader.loadTransformationMatrix(transformationMatrix);
            vbo.renderTextured(type * 4, 4);
            Drawer.spriteShader.stop();
        }
    }

    @Override
    public void renderPart(int partXStart, int partXEnd) {
        renderPart(0, partXStart, partXEnd);
    }


    @Override
    public void renderShadowPart(int partXStart, int partXEnd, float color) {
        renderShadowPart(0, partXStart, partXEnd, color);
    }

    @Override
    public void updateTexture(Player owner) {
    }

    @Override
    public void updateFrame() {
    }

    @Override
    public int getCurrentFrameIndex() {
        return 0;
    }

    @Override
    public int getXStart() {
        return 0;
    }

    @Override
    public int getYStart() {
        return 0;
    }

    @Override
    public int getActualWidth() {
        return width;
    }

    @Override
    public int getActualHeight() {
        return height;
    }

    @Override
    public int getXOffset() {
        return 0;
    }

    @Override
    public int getYOffset() {
        return 0;
    }

    protected int[] getBuffers() {
        int[] buffers = {frameBufferObject};
        return buffers;
    }

    protected int[] getTextures() {
        int[] textures = {texture};
        return textures;
    }
}