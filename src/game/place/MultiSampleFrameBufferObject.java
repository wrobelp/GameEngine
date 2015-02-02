package game.place;

import game.Settings;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.ARBTextureMultisample;
import org.lwjgl.opengl.EXTFramebufferObject;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class MultiSampleFrameBufferObject extends FrameBufferObject {

    private final int fboMS;
    private final int texture2;
    private final makeMultiSample[] makeMultiSamples;

    public MultiSampleFrameBufferObject(int w, int h, Settings settings) {
        super(w, h, settings);
        texture2 = glGenTextures();
        makeMultiSamples = new makeMultiSample[3];
        if (version == 0) {
            fboMS = GL30.glGenFramebuffers();
        } else if (version == 1) {
            fboMS = ARBFramebufferObject.glGenFramebuffers();
        } else {
            fboMS = EXTFramebufferObject.glGenFramebuffersEXT();
        }
        if (multiSampleSupported) {
            activates[0] = () -> {
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fboMS);
            };
            activates[1] = () -> {
                ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, fboMS);
            };
            deactivates[0] = () -> {
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fboMS);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameBufferObject);
                GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
            };
            deactivates[1] = () -> {
                ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, fboMS);
                ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, frameBufferObject);
                ARBFramebufferObject.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
                ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, 0);
            };
            makeMultiSamples[0] = (int nrSamples) -> {
                glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, texture2);
                GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, nrSamples, GL_RGBA8, width, height, false);
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboMS);
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D_MULTISAMPLE, texture2, 0);
            };
            makeMultiSamples[1] = new makeMultiSample() {

                @Override
                public void makeMultiSample(int nrSamples) {
                    glBindTexture(ARBTextureMultisample.GL_TEXTURE_2D_MULTISAMPLE, texture2);
                    ARBTextureMultisample.glTexImage2DMultisample(ARBTextureMultisample.GL_TEXTURE_2D_MULTISAMPLE, nrSamples, GL_RGBA8, width, height, false);
                    ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, fboMS);
                    ARBFramebufferObject.glFramebufferTexture2D(ARBFramebufferObject.GL_FRAMEBUFFER, ARBFramebufferObject.GL_COLOR_ATTACHMENT0, ARBTextureMultisample.GL_TEXTURE_2D_MULTISAMPLE, texture2, 0);
                }
            };
        } else {
            activates[0] = new activateType() {
                @Override
                public void activate() {
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameBufferObject);
                }
            };
            activates[1] = new activateType() {
                @Override
                public void activate() {
                    ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, frameBufferObject);
                }
            };
            deactivates[0] = new deactivateType() {
                @Override
                public void deactivate() {
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
                }
            };
            deactivates[1] = new deactivateType() {
                @Override
                public void deactivate() {
                    ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, 0);
                }
            };
            makeMultiSamples[0] = new makeMultiSample() {
                @Override
                public void makeMultiSample(int nrSamples) {
                }
            };
            makeMultiSamples[1] = new makeMultiSample() {

                @Override
                public void makeMultiSample(int nrSamples) {
                }
            };
        }
        activates[2] = new activateType() {
            @Override
            public void activate() {
                EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, frameBufferObject);
            }
        };
        deactivates[2] = new deactivateType() {
            @Override
            public void deactivate() {
                EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
            }
        };
        makeTextures[0] = new makeTextureType() {

            @Override
            public void makeTexture() {
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameBufferObject);
                glBindTexture(GL_TEXTURE_2D, texture);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_BYTE, BufferUtils.createByteBuffer(4 * width * height));
                GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
            }
        };
        makeTextures[1] = new makeTextureType() {

            @Override
            public void makeTexture() {
                ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, frameBufferObject);
                glBindTexture(GL_TEXTURE_2D, texture);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_BYTE, BufferUtils.createByteBuffer(4 * width * height));
                ARBFramebufferObject.glFramebufferTexture2D(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, ARBFramebufferObject.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
            }
        };
        makeTextures[2] = new makeTextureType() {

            @Override
            public void makeTexture() {
                EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, frameBufferObject);
                glBindTexture(GL_TEXTURE_2D, texture);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_BYTE, BufferUtils.createByteBuffer(4 * width * height));
                EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, texture, 0);
            }
        };
        makeMultiSamples[2] = new makeMultiSample() {

            @Override
            public void makeMultiSample(int nrSamples) {
            }
        };
        makeMultiSample(settings.nrSamples);
        makeTexture();
        if (version == 0) {
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
        } else if (version == 1) {
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, 0);
        } else {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        }
    }

    @Override
    public void activate() {
        activates[version].activate();
    }

    @Override
    public void deactivate() {
        deactivates[version].deactivate();
    }

    private void makeTexture() {
        makeTextures[version].makeTexture();
    }

    private void makeMultiSample(int nrSamples) {
        makeMultiSamples[version].makeMultiSample(nrSamples);
    }

    private interface makeMultiSample {

        void makeMultiSample(int nrSamples);
    }

}