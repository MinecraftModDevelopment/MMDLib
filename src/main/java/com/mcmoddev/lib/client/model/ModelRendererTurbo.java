package com.mcmoddev.lib.client.model;

import java.io.IOException;
import java.util.*;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An extension to the ModelRenderer class. It basically is a copy to ModelRenderer, however,
 * it contains various new methods to make your models. <br /><br /> Since the
 * ModelRendererTurbo class gets loaded during startup, the models made can be very complex.
 * This is why I can afford to add, for example, Wavefont OBJ support or have the addSprite
 * method, methods that add a lot of vertices and polygons. <p> This version of TMT was updated
 * and maintened/extended for 1.8+ MC by FEX___96
 *
 * @author GaryCXJk, Ferdinand (FEX___96)
 */
@SideOnly(Side.CLIENT)
public class ModelRendererTurbo extends ModelRenderer {

    public static final int MR_FRONT = 0;
    public static final int MR_BACK = 1;
    public static final int MR_LEFT = 2;
    public static final int MR_RIGHT = 3;
    public static final int MR_TOP = 4;
    public static final int MR_BOTTOM = 5;
    private static final float pi = (float) Math.PI;
    public final String boxName;
    public boolean mirror;
    public boolean flip;
    public boolean showModel;
    public boolean field_1402_i;
    public boolean forcedRecompile;
    public boolean useLegacyCompiler;
    public List cubeList;
    public List childModels;
    private PositionTextureVertex vertices[];
    private TexturedPolygon faces[];
    private int textureOffsetX;
    private int textureOffsetY;
    private boolean compiled;
    private int displayList;
    private int displayListArray[];
    private final Map<String, TransformGroup> transformGroup;
    private final Map<String, TextureGroup> textureGroup;
    private TransformGroup currentGroup;
    private TextureGroup currentTextureGroup;
    private String defaultTexture;

    public ModelRendererTurbo(ModelBase modelbase, String s) {
        super(modelbase, s);
        this.flip = false;
        this.compiled = false;
        this.displayList = 0;
        this.mirror = false;
        this.showModel = true;
        this.field_1402_i = false;
        this.vertices = new PositionTextureVertex[0];
        this.faces = new TexturedPolygon[0];
        this.forcedRecompile = false;
        this.transformGroup = new HashMap<>();
        this.transformGroup.put("0", new TransformGroupBone(new Bone(0, 0, 0, 0), 1D));
        this.textureGroup = new HashMap<>();
        this.textureGroup.put("0", new TextureGroup());
        this.currentTextureGroup = this.textureGroup.get("0");
        this.boxName = s;
        this.defaultTexture = "";
        this.useLegacyCompiler = false;
    }

    public ModelRendererTurbo(ModelBase modelbase) {
        this(modelbase, null);
    }

    /**
     * Creates a new ModelRenderTurbo object. It requires the coordinates of the position of
     * the texture.
     *
     * @param modelbase
     * @param textureX the x-coordinate on the texture
     * @param textureY the y-coordinate on the texture
     */
    public ModelRendererTurbo(ModelBase modelbase, int textureX, int textureY) {
        this(modelbase, textureX, textureY, 64, 32);
    }

    /**
     * Creates a new ModelRenderTurbo object. It requires the coordinates of the position of
     * the texture, but also allows you to specify the width and height of the texture,
     * allowing you to use bigger textures instead.
     *
     * @param modelbase
     * @param textureX
     * @param textureY
     * @param textureU
     * @param textureV
     */
    public ModelRendererTurbo(ModelBase modelbase, int textureX, int textureY, int textureU, int textureV) {
        this(modelbase);
        this.textureOffsetX = textureX;
        this.textureOffsetY = textureY;
        this.textureWidth = textureU;
        this.textureHeight = textureV;
    }

    /**
     * Creates a new polygon.
     *
     * @param verts an array of vertices
     */
    public void addPolygon (PositionTextureVertex[] verts) {
        this.copyTo(verts, new TexturedPolygon[] { new TexturedPolygon(verts) });
    }

    /**
     * Creates a new polygon, and adds UV mapping to it.
     *
     * @param verts an array of vertices
     * @param uv an array of UV coordinates
     */
    public void addPolygon (PositionTextureVertex[] verts, int[][] uv) {
        try {
            for (int i = 0; i < verts.length; i++)
                verts[i] = verts[i].setTexturePosition(uv[i][0] / this.textureWidth, uv[i][1] / this.textureHeight);
        }
        finally {
            this.addPolygon(verts);
        }
    }

    /**
     * Creates a new polygon with a given UV.
     *
     * @param verts an array of vertices
     * @param u1
     * @param v1
     * @param u2
     * @param v2
     */
    public void addPolygon (PositionTextureVertex[] verts, int u1, int v1, int u2, int v2) {
        this.copyTo(verts, new TexturedPolygon[] { this.addPolygonReturn(verts, u1, v1, u2, v2) });
    }

    private TexturedPolygon addPolygonReturn (PositionTextureVertex[] verts, int u1, int v1, int u2, int v2) {
        if (verts.length < 3)
            return null;
        final float uOffs = 1.0F / (this.textureWidth * 10.0F);
        final float vOffs = 1.0F / (this.textureHeight * 10.0F);
        if (verts.length < 4) {
            float xMin = -1;
            float yMin = -1;
            float xMax = 0;
            float yMax = 0;
            for (final PositionTextureVertex vert : verts) {
                final float xPos = vert.texturePositionX;
                final float yPos = vert.texturePositionY;
                xMax = Math.max(xMax, xPos);
                xMin = xMin < -1 ? xPos : Math.min(xMin, xPos);
                yMax = Math.max(yMax, yPos);
                yMin = yMin < -1 ? yPos : Math.min(yMin, yPos);
            }
            final float uMin = u1 / this.textureWidth + uOffs;
            final float vMin = v1 / this.textureHeight + vOffs;
            final float uSize = (u2 - u1) / this.textureWidth - uOffs * 2;
            final float vSize = (v2 - v1) / this.textureHeight - vOffs * 2;
            final float xSize = xMax - xMin;
            final float ySize = yMax - yMin;
            for (int i = 0; i < verts.length; i++) {
                float xPos = verts[i].texturePositionX;
                float yPos = verts[i].texturePositionY;
                xPos = (xPos - xMin) / xSize;
                yPos = (yPos - yMin) / ySize;
                verts[i] = verts[i].setTexturePosition(uMin + xPos * uSize, vMin + yPos * vSize);
            }
        }
        else {
            verts[0] = verts[0].setTexturePosition(u2 / this.textureWidth - uOffs, v1 / this.textureHeight + vOffs);
            verts[1] = verts[1].setTexturePosition(u1 / this.textureWidth + uOffs, v1 / this.textureHeight + vOffs);
            verts[2] = verts[2].setTexturePosition(u1 / this.textureWidth + uOffs, v2 / this.textureHeight - vOffs);
            verts[3] = verts[3].setTexturePosition(u2 / this.textureWidth - uOffs, v2 / this.textureHeight - vOffs);
        }
        return new TexturedPolygon(verts);
    }

    /**
     * Adds a rectangular shape. Basically, you can make any eight-pointed shape you want, as
     * the method requires eight vector coordinates.
     *
     * @param v a float array with three values, the x, y and z coordinates of the vertex
     * @param v1 a float array with three values, the x, y and z coordinates of the vertex
     * @param v2 a float array with three values, the x, y and z coordinates of the vertex
     * @param v3 a float array with three values, the x, y and z coordinates of the vertex
     * @param v4 a float array with three values, the x, y and z coordinates of the vertex
     * @param v5 a float array with three values, the x, y and z coordinates of the vertex
     * @param v6 a float array with three values, the x, y and z coordinates of the vertex
     * @param v7 a float array with three values, the x, y and z coordinates of the vertex
     * @param w the width of the shape, used in determining the texture
     * @param h the height of the shape, used in determining the texture
     * @param d the depth of the shape, used in determining the texture
     */
    public void addRectShape (float[] v, float[] v1, float[] v2, float[] v3, float[] v4, float[] v5, float[] v6, float[] v7, int w, int h, int d) {
        final PositionTextureVertex[] verts = new PositionTextureVertex[8];
        final TexturedPolygon[] poly = new TexturedPolygon[6];
        final PositionTextureVertex positionTexturevertex = new PositionTextureVertex(v[0], v[1], v[2], 0.0F, 0.0F);
        final PositionTextureVertex positionTexturevertex1 = new PositionTextureVertex(v1[0], v1[1], v1[2], 0.0F, 8F);
        final PositionTextureVertex positionTexturevertex2 = new PositionTextureVertex(v2[0], v2[1], v2[2], 8F, 8F);
        final PositionTextureVertex positionTexturevertex3 = new PositionTextureVertex(v3[0], v3[1], v3[2], 8F, 0.0F);
        final PositionTextureVertex positionTexturevertex4 = new PositionTextureVertex(v4[0], v4[1], v4[2], 0.0F, 0.0F);
        final PositionTextureVertex positionTexturevertex5 = new PositionTextureVertex(v5[0], v5[1], v5[2], 0.0F, 8F);
        final PositionTextureVertex positionTexturevertex6 = new PositionTextureVertex(v6[0], v6[1], v6[2], 8F, 8F);
        final PositionTextureVertex positionTexturevertex7 = new PositionTextureVertex(v7[0], v7[1], v7[2], 8F, 0.0F);
        verts[0] = positionTexturevertex;
        verts[1] = positionTexturevertex1;
        verts[2] = positionTexturevertex2;
        verts[3] = positionTexturevertex3;
        verts[4] = positionTexturevertex4;
        verts[5] = positionTexturevertex5;
        verts[6] = positionTexturevertex6;
        verts[7] = positionTexturevertex7;
        poly[0] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex5, positionTexturevertex1, positionTexturevertex2, positionTexturevertex6 }, this.textureOffsetX + d + w, this.textureOffsetY + d, this.textureOffsetX + d + w + d, this.textureOffsetY + d + h);
        poly[1] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex, positionTexturevertex4, positionTexturevertex7, positionTexturevertex3 }, this.textureOffsetX + 0, this.textureOffsetY + d, this.textureOffsetX + d, this.textureOffsetY + d + h);
        poly[2] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex5, positionTexturevertex4, positionTexturevertex, positionTexturevertex1 }, this.textureOffsetX + d, this.textureOffsetY + 0, this.textureOffsetX + d + w, this.textureOffsetY + d);
        poly[3] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex2, positionTexturevertex3, positionTexturevertex7, positionTexturevertex6 }, this.textureOffsetX + d + w, this.textureOffsetY + 0, this.textureOffsetX + d + w + w, this.textureOffsetY + d);
        poly[4] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex1, positionTexturevertex, positionTexturevertex3, positionTexturevertex2 }, this.textureOffsetX + d, this.textureOffsetY + d, this.textureOffsetX + d + w, this.textureOffsetY + d + h);
        poly[5] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex4, positionTexturevertex5, positionTexturevertex6, positionTexturevertex7 }, this.textureOffsetX + d + w + d, this.textureOffsetY + d, this.textureOffsetX + d + w + d + w, this.textureOffsetY + d + h);
        if (this.mirror ^ this.flip)
            for (final TexturedPolygon element : poly)
                element.flipFace();
        this.copyTo(verts, poly);
    }

    /**
     * Adds a new box to the model.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param w the width (over the x-direction)
     * @param h the height (over the y-direction)
     * @param d the depth (over the z-direction)
     */
    @Override
    public ModelRendererTurbo addBox (float x, float y, float z, int w, int h, int d) {
        this.addBox(x, y, z, w, h, d, 0.0F);
        return this;
    }

    /**
     * Adds a new box to the model.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param w the width (over the x-direction)
     * @param h the height (over the y-direction)
     * @param d the depth (over the z-direction)
     * @param expansion the expansion of the box. It increases the size in each direction by
     *        that many.
     */
    @Override
    public void addBox (float x, float y, float z, int w, int h, int d, float expansion) {
        this.addBox(x, y, z, w, h, d, expansion, 1F);
    }

    /**
     * Adds a new box to the model.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param w the width (over the x-direction)
     * @param h the height (over the y-direction)
     * @param d the depth (over the z-direction)
     * @param expansion the expansion of the box. It increases the size in each direction by
     *        that many. It's independent from the scale.
     * @param scale
     */
    public void addBox (float x, float y, float z, int w, int h, int d, float expansion, float scale) {
        final float scaleX = w * scale;
        final float scaleY = h * scale;
        final float scaleZ = d * scale;
        float x1 = x + scaleX;
        float y1 = y + scaleY;
        float z1 = z + scaleZ;
        final float expX = expansion + scaleX - w;
        final float expY = expansion + scaleY - h;
        final float expZ = expansion + scaleZ - d;
        x -= expX;
        y -= expY;
        z -= expZ;
        x1 += expansion;
        y1 += expansion;
        z1 += expansion;
        if (this.mirror) {
            final float xTemp = x1;
            x1 = x;
            x = xTemp;
        }
        final float[] v = { x, y, z };
        final float[] v1 = { x1, y, z };
        final float[] v2 = { x1, y1, z };
        final float[] v3 = { x, y1, z };
        final float[] v4 = { x, y, z1 };
        final float[] v5 = { x1, y, z1 };
        final float[] v6 = { x1, y1, z1 };
        final float[] v7 = { x, y1, z1 };
        this.addRectShape(v, v1, v2, v3, v4, v5, v6, v7, w, h, d);
    }

    /**
     * Adds a trapezoid-like shape. It's achieved by expanding the shape on one side. You can
     * use the static variables <code>MR_RIGHT</code>, <code>MR_LEFT</code>,
     * <code>MR_FRONT</code>, <code>MR_BACK</code>, <code>MR_TOP</code> and
     * <code>MR_BOTTOM</code>.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param w the width (over the x-direction)
     * @param h the height (over the y-direction)
     * @param d the depth (over the z-direction)
     * @param scale the "scale" of the box. It only increases the size in each direction by
     *        that many.
     * @param bottomScale the "scale" of the bottom
     * @param dir the side the scaling is applied to
     */
    public void addTrapezoid (float x, float y, float z, int w, int h, int d, float scale, float bottomScale, int dir) {
        float f4 = x + w;
        float f5 = y + h;
        float f6 = z + d;
        x -= scale;
        y -= scale;
        z -= scale;
        f4 += scale;
        f5 += scale;
        f6 += scale;
        final int m = this.mirror ? -1 : 1;
        if (this.mirror) {
            final float f7 = f4;
            f4 = x;
            x = f7;
        }
        final float[] v = { x, y, z };
        final float[] v1 = { f4, y, z };
        final float[] v2 = { f4, f5, z };
        final float[] v3 = { x, f5, z };
        final float[] v4 = { x, y, f6 };
        final float[] v5 = { f4, y, f6 };
        final float[] v6 = { f4, f5, f6 };
        final float[] v7 = { x, f5, f6 };
        switch (dir) {
            case MR_RIGHT:
                v[1] -= bottomScale;
                v[2] -= bottomScale;
                v3[1] += bottomScale;
                v3[2] -= bottomScale;
                v4[1] -= bottomScale;
                v4[2] += bottomScale;
                v7[1] += bottomScale;
                v7[2] += bottomScale;
                break;
            case MR_LEFT:
                v1[1] -= bottomScale;
                v1[2] -= bottomScale;
                v2[1] += bottomScale;
                v2[2] -= bottomScale;
                v5[1] -= bottomScale;
                v5[2] += bottomScale;
                v6[1] += bottomScale;
                v6[2] += bottomScale;
                break;
            case MR_FRONT:
                v[0] -= m * bottomScale;
                v[1] -= bottomScale;
                v1[0] += m * bottomScale;
                v1[1] -= bottomScale;
                v2[0] += m * bottomScale;
                v2[1] += bottomScale;
                v3[0] -= m * bottomScale;
                v3[1] += bottomScale;
                break;
            case MR_BACK:
                v4[0] -= m * bottomScale;
                v4[1] -= bottomScale;
                v5[0] += m * bottomScale;
                v5[1] -= bottomScale;
                v6[0] += m * bottomScale;
                v6[1] += bottomScale;
                v7[0] -= m * bottomScale;
                v7[1] += bottomScale;
                break;
            case MR_TOP:
                v[0] -= m * bottomScale;
                v[2] -= bottomScale;
                v1[0] += m * bottomScale;
                v1[2] -= bottomScale;
                v4[0] -= m * bottomScale;
                v4[2] += bottomScale;
                v5[0] += m * bottomScale;
                v5[2] += bottomScale;
                break;
            case MR_BOTTOM:
                v2[0] += m * bottomScale;
                v2[2] -= bottomScale;
                v3[0] -= m * bottomScale;
                v3[2] -= bottomScale;
                v6[0] += m * bottomScale;
                v6[2] += bottomScale;
                v7[0] -= m * bottomScale;
                v7[2] += bottomScale;
                break;
        }
        this.addRectShape(v, v1, v2, v3, v4, v5, v6, v7, w, h, d);
    }

    /**
     * Creates a shape from a 2D vector shape.
     *
     * @param x the starting x position
     * @param y the starting y position
     * @param z the starting z position
     * @param coordinates an array of coordinates that form the shape
     * @param depth the depth of the shape
     * @param shapeTextureWidth the width of the texture of one side of the shape
     * @param shapeTextureHeight the height of the texture the shape
     * @param sideTextureWidth the width of the texture of the side of the shape
     * @param sideTextureHeight the height of the texture of the side of the shape
     * @param direction the direction the starting point of the shape is facing
     */
    public void addShape3D (float x, float y, float z, Coord2D[] coordinates, float depth, int shapeTextureWidth, int shapeTextureHeight, int sideTextureWidth, int sideTextureHeight, int direction) {
        this.addShape3D(x, y, z, coordinates, depth, shapeTextureWidth, shapeTextureHeight, sideTextureWidth, sideTextureHeight, direction, null);
    }

    /**
     * Creates a shape from a 2D vector shape.
     *
     * @param x the starting x position
     * @param y the starting y position
     * @param z the starting z position
     * @param coordinates an array of coordinates that form the shape
     * @param depth the depth of the shape
     * @param shapeTextureWidth the width of the texture of one side of the shape
     * @param shapeTextureHeight the height of the texture the shape
     * @param sideTextureWidth the width of the texture of the side of the shape
     * @param sideTextureHeight the height of the texture of the side of the shape
     * @param direction the direction the starting point of the shape is facing
     * @param faceLengths An array with the length of each face. Used to set the texture width
     *        of each face on the side manually.
     */
    public void addShape3D (float x, float y, float z, Coord2D[] coordinates, float depth, int shapeTextureWidth, int shapeTextureHeight, int sideTextureWidth, int sideTextureHeight, int direction, float[] faceLengths) {
        this.addShape3D(x, y, z, new Shape2D(coordinates), depth, shapeTextureWidth, shapeTextureHeight, sideTextureWidth, sideTextureHeight, direction, faceLengths);
    }

    /**
     * Creates a shape from a 2D vector shape.
     *
     * @param x the starting x position
     * @param y the starting y position
     * @param z the starting z position
     * @param coordinates an ArrayList of coordinates that form the shape
     * @param depth the depth of the shape
     * @param shapeTextureWidth the width of the texture of one side of the shape
     * @param shapeTextureHeight the height of the texture the shape
     * @param sideTextureWidth the width of the texture of the side of the shape
     * @param sideTextureHeight the height of the texture of the side of the shape
     * @param direction the direction the starting point of the shape is facing
     */
    public void addShape3D (float x, float y, float z, ArrayList<Coord2D> coordinates, float depth, int shapeTextureWidth, int shapeTextureHeight, int sideTextureWidth, int sideTextureHeight, int direction) {
        this.addShape3D(x, y, z, coordinates, depth, shapeTextureWidth, shapeTextureHeight, sideTextureWidth, sideTextureHeight, direction, null);
    }

    /**
     * Creates a shape from a 2D vector shape.
     *
     * @param x the starting x position
     * @param y the starting y position
     * @param z the starting z position
     * @param coordinates an ArrayList of coordinates that form the shape
     * @param depth the depth of the shape
     * @param shapeTextureWidth the width of the texture of one side of the shape
     * @param shapeTextureHeight the height of the texture the shape
     * @param sideTextureWidth the width of the texture of the side of the shape
     * @param sideTextureHeight the height of the texture of the side of the shape
     * @param direction the direction the starting point of the shape is facing
     * @param faceLengths An array with the length of each face. Used to set the texture width
     *        of each face on the side manually.
     */
    public void addShape3D (float x, float y, float z, ArrayList<Coord2D> coordinates, float depth, int shapeTextureWidth, int shapeTextureHeight, int sideTextureWidth, int sideTextureHeight, int direction, float[] faceLengths) {
        this.addShape3D(x, y, z, new Shape2D(coordinates), depth, shapeTextureWidth, shapeTextureHeight, sideTextureWidth, sideTextureHeight, direction, faceLengths);
    }

    /**
     * Creates a shape from a 2D vector shape.
     *
     * @param x the starting x position
     * @param y the starting y position
     * @param z the starting z position
     * @param shape a Shape2D which contains the coordinates of the shape points
     * @param depth the depth of the shape
     * @param shapeTextureWidth the width of the texture of one side of the shape
     * @param shapeTextureHeight the height of the texture the shape
     * @param sideTextureWidth the width of the texture of the side of the shape
     * @param sideTextureHeight the height of the texture of the side of the shape
     * @param direction the direction the starting point of the shape is facing
     */
    public void addShape3D (float x, float y, float z, Shape2D shape, float depth, int shapeTextureWidth, int shapeTextureHeight, int sideTextureWidth, int sideTextureHeight, int direction) {
        this.addShape3D(x, y, z, shape, depth, shapeTextureWidth, shapeTextureHeight, sideTextureWidth, sideTextureHeight, direction, null);
    }

    /**
     * Creates a shape from a 2D vector shape.
     *
     * @param x the starting x position
     * @param y the starting y position
     * @param z the starting z position
     * @param shape a Shape2D which contains the coordinates of the shape points
     * @param depth the depth of the shape
     * @param shapeTextureWidth the width of the texture of one side of the shape
     * @param shapeTextureHeight the height of the texture the shape
     * @param sideTextureWidth the width of the texture of the side of the shape
     * @param sideTextureHeight the height of the texture of the side of the shape
     * @param direction the direction the starting point of the shape is facing
     * @param faceLengths An array with the length of each face. Used to set the texture width
     *        of each face on the side manually.
     */
    public void addShape3D (float x, float y, float z, Shape2D shape, float depth, int shapeTextureWidth, int shapeTextureHeight, int sideTextureWidth, int sideTextureHeight, int direction, float[] faceLengths) {
        float rotX = 0;
        float rotY = 0;
        final float rotZ = 0;
        switch (direction) {
            case MR_LEFT:
                rotY = pi / 2;
                break;
            case MR_RIGHT:
                rotY = -pi / 2;
                break;
            case MR_TOP:
                rotX = pi / 2;
                break;
            case MR_BOTTOM:
                rotX = -pi / 2;
                break;
            case MR_FRONT:
                rotY = pi;
                break;
            case MR_BACK:
                break;
        }
        this.addShape3D(x, y, z, shape, depth, shapeTextureWidth, shapeTextureHeight, sideTextureWidth, sideTextureHeight, rotX, rotY, rotZ, faceLengths);
    }

    /**
     * Creates a shape from a 2D vector shape.
     *
     * @param x the starting x position
     * @param y the starting y position
     * @param z the starting z position
     * @param shape a Shape2D which contains the coordinates of the shape points
     * @param depth the depth of the shape
     * @param shapeTextureWidth the width of the texture of one side of the shape
     * @param shapeTextureHeight the height of the texture the shape
     * @param sideTextureWidth the width of the texture of the side of the shape
     * @param sideTextureHeight the height of the texture of the side of the shape
     * @param rotX the rotation around the x-axis
     * @param rotY the rotation around the y-axis
     * @param rotZ the rotation around the z-axis
     */
    public void addShape3D (float x, float y, float z, Shape2D shape, float depth, int shapeTextureWidth, int shapeTextureHeight, int sideTextureWidth, int sideTextureHeight, float rotX, float rotY, float rotZ) {
        this.addShape3D(x, y, z, shape, depth, shapeTextureWidth, shapeTextureHeight, sideTextureWidth, sideTextureHeight, rotX, rotY, rotZ, null);
    }

    public void addShape3D (float x, float y, float z, Shape2D shape, float depth, int shapeTextureWidth, int shapeTextureHeight, int sideTextureWidth, int sideTextureHeight, float rotX, float rotY, float rotZ, float[] faceLengths) {
        final Shape3D shape3D = shape.extrude(x, y, z, rotX, rotY, rotZ, depth, this.textureOffsetX, this.textureOffsetY, this.textureWidth, this.textureHeight, shapeTextureWidth, shapeTextureHeight, sideTextureWidth, sideTextureHeight, faceLengths);
        if (this.flip)
            for (final TexturedPolygon face : shape3D.faces)
                face.flipFace();
        this.copyTo(shape3D.vertices, shape3D.faces);
    }

    /**
     * Adds a cube the size of one pixel. It will take a pixel from the texture and uses that
     * as the texture of said cube. The accurate name would actually be "addVoxel". This method
     * has been added to make it more compatible with Techne, and allows for easy
     * single-colored boxes.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param width the width of the box
     * @param height the height of the box
     * @param length the length of the box
     */
    public void addPixel (float x, float y, float z, float width, float height, float length) {
        this.addPixel(x, y, z, new float[] { width, height, length }, this.textureOffsetX, this.textureOffsetY);
    }

    /**
     * Adds a cube the size of one pixel. It will take a pixel from the texture and uses that
     * as the texture of said cube. The accurate name would actually be "addVoxel". It will not
     * overwrite the model data, but rather, it will add to the model.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param scale the "scale" of the cube, where scale is a float integer consisting of three
     *        values
     * @param w the x-coordinate on the texture
     * @param h the y-coordinate on the texture
     */
    public void addPixel (float x, float y, float z, float[] scale, int w, int h) {
        final PositionTextureVertex[] verts = new PositionTextureVertex[8];
        final TexturedPolygon[] poly = new TexturedPolygon[6];
        final float x1 = x + scale[0];
        final float y1 = y + scale[1];
        final float z1 = z + scale[2];
        final float[] f = { x, y, z };
        final float[] f1 = { x1, y, z };
        final float[] f2 = { x1, y1, z };
        final float[] f3 = { x, y1, z };
        final float[] f4 = { x, y, z1 };
        final float[] f5 = { x1, y, z1 };
        final float[] f6 = { x1, y1, z1 };
        final float[] f7 = { x, y1, z1 };
        final PositionTextureVertex positionTexturevertex = new PositionTextureVertex(f[0], f[1], f[2], 0.0F, 0.0F);
        final PositionTextureVertex positionTexturevertex1 = new PositionTextureVertex(f1[0], f1[1], f1[2], 0.0F, 8F);
        final PositionTextureVertex positionTexturevertex2 = new PositionTextureVertex(f2[0], f2[1], f2[2], 8F, 8F);
        final PositionTextureVertex positionTexturevertex3 = new PositionTextureVertex(f3[0], f3[1], f3[2], 8F, 0.0F);
        final PositionTextureVertex positionTexturevertex4 = new PositionTextureVertex(f4[0], f4[1], f4[2], 0.0F, 0.0F);
        final PositionTextureVertex positionTexturevertex5 = new PositionTextureVertex(f5[0], f5[1], f5[2], 0.0F, 8F);
        final PositionTextureVertex positionTexturevertex6 = new PositionTextureVertex(f6[0], f6[1], f6[2], 8F, 8F);
        final PositionTextureVertex positionTexturevertex7 = new PositionTextureVertex(f7[0], f7[1], f7[2], 8F, 0.0F);
        verts[0] = positionTexturevertex;
        verts[1] = positionTexturevertex1;
        verts[2] = positionTexturevertex2;
        verts[3] = positionTexturevertex3;
        verts[4] = positionTexturevertex4;
        verts[5] = positionTexturevertex5;
        verts[6] = positionTexturevertex6;
        verts[7] = positionTexturevertex7;
        poly[0] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex5, positionTexturevertex1, positionTexturevertex2, positionTexturevertex6 }, w, h, w + 1, h + 1);
        poly[1] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex, positionTexturevertex4, positionTexturevertex7, positionTexturevertex3 }, w, h, w + 1, h + 1);
        poly[2] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex5, positionTexturevertex4, positionTexturevertex, positionTexturevertex1 }, w, h, w + 1, h + 1);
        poly[3] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex2, positionTexturevertex3, positionTexturevertex7, positionTexturevertex6 }, w, h, w + 1, h + 1);
        poly[4] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex1, positionTexturevertex, positionTexturevertex3, positionTexturevertex2 }, w, h, w + 1, h + 1);
        poly[5] = this.addPolygonReturn(new PositionTextureVertex[] { positionTexturevertex4, positionTexturevertex5, positionTexturevertex6, positionTexturevertex7 }, w, h, w + 1, h + 1);
        this.copyTo(verts, poly);
    }

    /**
     * Creates a model shaped like the exact image on the texture. Note that this method will
     * increase the amount of quads on your model, which could effectively slow down your PC,
     * so unless it is really a necessity to use it, I'd suggest you avoid using this method to
     * create your model.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param w the width of the sprite
     * @param h the height of the sprite
     * @param expansion the expansion of the sprite. It only increases the size in each
     *        direction by that many.
     */
    public void addSprite (float x, float y, float z, int w, int h, float expansion) {
        this.addSprite(x, y, z, w, h, 1, false, false, false, false, false, expansion);
    }

    /**
     * Creates a model shaped like the exact image on the texture. Note that this method will
     * increase the amount of quads on your model, which could effectively slow down your PC,
     * so unless it is really a necessity to use it, I'd suggest you avoid using this method to
     * create your model.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param w the width of the sprite
     * @param h the height of the sprite
     * @param rotX a boolean to define if it rotates 90 degrees around its yaw-axis
     * @param rotY a boolean to define if it rotates 90 degrees around its pitch-axis
     * @param rotZ a boolean to define if it rotates 90 degrees around its roll-axis
     * @param mirrorX a boolean to define if the sprite should be mirrored
     * @param mirrorY a boolean to define if the sprite should be flipped
     * @param expansion the expansion of the sprite. It only increases the size in each
     *        direction by that many.
     */
    public void addSprite (float x, float y, float z, int w, int h, boolean rotX, boolean rotY, boolean rotZ, boolean mirrorX, boolean mirrorY, float expansion) {
        this.addSprite(x, y, z, w, h, 1, rotX, rotY, rotZ, mirrorX, mirrorY, expansion);
    }

    /**
     * Creates a model shaped like the exact image on the texture. Note that this method will
     * increase the amount of quads on your model, which could effectively slow down your PC,
     * so unless it is really a necessity to use it, I'd suggest you avoid using this method to
     * create your model.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param w the width of the sprite
     * @param h the height of the sprite
     * @param d the depth of the shape itself
     * @param rotX a boolean to define if it rotates 90 degrees around its yaw-axis
     * @param rotY a boolean to define if it rotates 90 degrees around its pitch-axis
     * @param rotZ a boolean to define if it rotates 90 degrees around its roll-axis
     * @param mirrorX a boolean to define if the sprite should be mirrored
     * @param mirrorY a boolean to define if the sprite should be flipped
     * @param expansion the expansion of the sprite. It only increases the size in each
     *        direction by that many.
     */
    public void addSprite (float x, float y, float z, int w, int h, int d, boolean rotX, boolean rotY, boolean rotZ, boolean mirrorX, boolean mirrorY, float expansion) {
        this.addSprite(x, y, z, w, h, d, 1.0F, rotX, rotY, rotZ, mirrorX, mirrorY, expansion);
    }

    /**
     * Creates a model shaped like the exact image on the texture. Note that this method will
     * increase the amount of quads on your model, which could effectively slow down your PC,
     * so unless it is really a necessity to use it, I'd suggest you avoid using this method to
     * create your model.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param w the width of the sprite
     * @param h the height of the sprite
     * @param d the depth of the shape itself
     * @param pixelScale the scale of each individual pixel
     * @param rotX a boolean to define if it rotates 90 degrees around its yaw-axis
     * @param rotY a boolean to define if it rotates 90 degrees around its pitch-axis
     * @param rotZ a boolean to define if it rotates 90 degrees around its roll-axis
     * @param mirrorX a boolean to define if the sprite should be mirrored
     * @param mirrorY a boolean to define if the sprite should be flipped
     * @param expansion the expansion of the sprite. It only increases the size in each
     *        direction by that many.
     */
    public void addSprite (float x, float y, float z, int w, int h, int d, float pixelScale, boolean rotX, boolean rotY, boolean rotZ, boolean mirrorX, boolean mirrorY, float expansion) {
        final String[] mask = new String[h];
        final char[] str = new char[w];
        Arrays.fill(str, '1');
        Arrays.fill(mask, new String(str));
        this.addSprite(x, y, z, mask, d, pixelScale, rotX, rotY, rotZ, mirrorX, mirrorY, expansion);
    }

    /**
     * Creates a model shaped like the exact image on the texture. Note that this method will
     * increase the amount of quads on your model, which could effectively slow down your PC,
     * so unless it is really a necessity to use it, I'd suggest you avoid using this method to
     * create your model. <br /><br /> This method uses a mask string. This way you can reduce
     * the amount of quads used. To use this, create a String array, where you use a 1 to
     * signify that the pixel will be drawn. Any other character will cause that pixel to not
     * be drawn.
     *
     * @param x the starting x-position
     * @param y the starting y-position
     * @param z the starting z-position
     * @param mask an array with the mask string
     * @param d the depth of the shape itself
     * @param pixelScale the scale of each individual pixel
     * @param rotX a boolean to define if it rotates 90 degrees around its yaw-axis
     * @param rotY a boolean to define if it rotates 90 degrees around its pitch-axis
     * @param rotZ a boolean to define if it rotates 90 degrees around its roll-axis
     * @param mirrorX a boolean to define if the sprite should be mirrored
     * @param mirrorY a boolean to define if the sprite should be flipped
     * @param expansion the expansion of the sprite. It only increases the size in each
     *        direction by that many.
     */
    public void addSprite (float x, float y, float z, String[] mask, int d, float pixelScale, boolean rotX, boolean rotY, boolean rotZ, boolean mirrorX, boolean mirrorY, float expansion) {
        final int w = mask[0].length();
        final int h = mask.length;
        final float x1 = x - expansion;
        final float y1 = y - expansion;
        final float z1 = z - expansion;
        int wDir = 0;
        int hDir = 0;
        int dDir = 0;
        final float wScale = 1F + expansion / (w * pixelScale);
        final float hScale = 1F + expansion / (h * pixelScale);
        if (!rotX) {
            if (!rotY) {
                if (!rotZ) {
                    wDir = 0;
                    hDir = 1;
                    dDir = 2;
                }
                else {
                    wDir = 1;
                    hDir = 0;
                    dDir = 2;
                }
            }
            else if (!rotZ) {
                wDir = 2;
                hDir = 1;
                dDir = 0;
            }
            else {
                wDir = 2;
                hDir = 0;
                dDir = 1;
            }
        }
        else if (!rotY) {
            if (!rotZ) {
                wDir = 0;
                hDir = 2;
                dDir = 1;
            }
            else {
                wDir = 1;
                hDir = 2;
                dDir = 0;
            }
        }
        else if (!rotZ) {
            wDir = 2;
            hDir = 0;
            dDir = 1;
        }
        else {
            wDir = 2;
            hDir = 1;
            dDir = 0;
        }
        final int texStartX = this.textureOffsetX + (mirrorX ? w * 1 - 1 : 0);
        final int texStartY = this.textureOffsetY + (mirrorY ? h * 1 - 1 : 0);
        final int texDirX = mirrorX ? -1 : 1;
        final int texDirY = mirrorY ? -1 : 1;
        final float wVoxSize = this.getPixelSize(wScale, hScale, d * pixelScale + expansion * 2, 0, 1, wDir, 1, 1);
        final float hVoxSize = this.getPixelSize(wScale, hScale, d * pixelScale + expansion * 2, 0, 1, hDir, 1, 1);
        final float dVoxSize = this.getPixelSize(wScale, hScale, d * pixelScale + expansion * 2, 0, 1, dDir, 1, 1);
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
                if (mask[j].charAt(i) == '1')
                    this.addPixel(x1 + this.getPixelSize(wScale, hScale, 0, wDir, hDir, 0, i, j), y1 + this.getPixelSize(wScale, hScale, 0, wDir, hDir, 1, i, j), z1 + this.getPixelSize(wScale, hScale, 0, wDir, hDir, 2, i, j), new float[] { wVoxSize, hVoxSize, dVoxSize }, texStartX + texDirX * i, texStartY + texDirY * j);
    }

    private float getPixelSize (float wScale, float hScale, float dScale, int wDir, int hDir, int checkDir, int texPosX, int texPosY) {
        return wDir == checkDir ? wScale * texPosX : hDir == checkDir ? hScale * texPosY : dScale;
    }

    /**
     * Adds a spherical shape.
     *
     * @param x
     * @param y
     * @param z
     * @param r
     * @param segs
     * @param rings
     * @param textureW
     * @param textureH
     */
    public void addSphere (float x, float y, float z, float r, int segs, int rings, int textureW, int textureH) {
        if (segs < 3)
            segs = 3;
        rings++;
        final PositionTextureVertex[] tempVerts = new PositionTextureVertex[segs * (rings - 1) + 2];
        final TexturedPolygon[] poly = new TexturedPolygon[segs * rings];
        tempVerts[0] = new PositionTextureVertex(x, y - r, z, 0, 0);
        tempVerts[tempVerts.length - 1] = new PositionTextureVertex(x, y + r, z, 0, 0);
        final float uOffs = 1.0F / (this.textureWidth * 10.0F);
        final float vOffs = 1.0F / (this.textureHeight * 10.0F);
        final float texW = textureW / this.textureWidth - 2F * uOffs;
        final float texH = textureH / this.textureHeight - 2F * vOffs;
        final float segW = texW / segs;
        final float segH = texH / rings;
        final float startU = this.textureOffsetX / this.textureWidth;
        final float startV = this.textureOffsetY / this.textureHeight;
        int currentFace = 0;
        for (int j = 1; j < rings; j++) {
            for (int i = 0; i < segs; i++) {
                final float yWidth = MathHelper.cos(-pi / 2 + pi / rings * j);
                final float yHeight = MathHelper.sin(-pi / 2 + pi / rings * j);
                final float xSize = MathHelper.sin(pi / segs * i * 2F + pi) * yWidth;
                final float zSize = -MathHelper.cos(pi / segs * i * 2F + pi) * yWidth;
                final int curVert = 1 + i + segs * (j - 1);
                tempVerts[curVert] = new PositionTextureVertex(x + xSize * r, y + yHeight * r, z + zSize * r, 0, 0);
                if (i > 0) {
                    PositionTextureVertex[] verts;
                    if (j == 1) {
                        verts = new PositionTextureVertex[4];
                        verts[0] = tempVerts[curVert].setTexturePosition(startU + segW * i, startV + segH * j);
                        verts[1] = tempVerts[curVert - 1].setTexturePosition(startU + segW * (i - 1), startV + segH * j);
                        verts[2] = tempVerts[0].setTexturePosition(startU + segW * (i - 1), startV);
                        verts[3] = tempVerts[0].setTexturePosition(startU + segW + segW * i, startV);
                    }
                    else {
                        verts = new PositionTextureVertex[4];
                        verts[0] = tempVerts[curVert].setTexturePosition(startU + segW * i, startV + segH * j);
                        verts[1] = tempVerts[curVert - 1].setTexturePosition(startU + segW * (i - 1), startV + segH * j);
                        verts[2] = tempVerts[curVert - 1 - segs].setTexturePosition(startU + segW * (i - 1), startV + segH * (j - 1));
                        verts[3] = tempVerts[curVert - segs].setTexturePosition(startU + segW * i, startV + segH * (j - 1));
                    }
                    poly[currentFace] = new TexturedPolygon(verts);
                    currentFace++;
                }
            }
            PositionTextureVertex[] verts;
            if (j == 1) {
                verts = new PositionTextureVertex[4];
                verts[0] = tempVerts[1].setTexturePosition(startU + segW * segs, startV + segH * j);
                verts[1] = tempVerts[segs].setTexturePosition(startU + segW * (segs - 1), startV + segH * j);
                verts[2] = tempVerts[0].setTexturePosition(startU + segW * (segs - 1), startV);
                verts[3] = tempVerts[0].setTexturePosition(startU + segW * segs, startV);
            }
            else {
                verts = new PositionTextureVertex[4];
                verts[0] = tempVerts[1 + segs * (j - 1)].setTexturePosition(startU + texW, startV + segH * j);
                verts[1] = tempVerts[segs * (j - 1) + segs].setTexturePosition(startU + texW - segW, startV + segH * j);
                verts[2] = tempVerts[segs * (j - 1)].setTexturePosition(startU + texW - segW, startV + segH * (j - 1));
                verts[3] = tempVerts[1 + segs * (j - 1) - segs].setTexturePosition(startU + texW, startV + segH * (j - 1));
            }
            poly[currentFace] = new TexturedPolygon(verts);
            currentFace++;
        }
        for (int i = 0; i < segs; i++) {
            final PositionTextureVertex[] verts = new PositionTextureVertex[3];
            final int curVert = tempVerts.length - (segs + 1);
            verts[0] = tempVerts[tempVerts.length - 1].setTexturePosition(startU + segW * (i + 0.5F), startV + texH);
            verts[1] = tempVerts[curVert + i].setTexturePosition(startU + segW * i, startV + texH - segH);
            verts[2] = tempVerts[curVert + (i + 1) % segs].setTexturePosition(startU + segW * (i + 1), startV + texH - segH);
            poly[currentFace] = new TexturedPolygon(verts);
            currentFace++;
        }
        this.copyTo(tempVerts, poly);
    }

    /**
     * Adds a cone.
     *
     * @param x the x-position of the base
     * @param y the y-position of the base
     * @param z the z-position of the base
     * @param radius the radius of the cylinder
     * @param length the length of the cylinder
     * @param segments the amount of segments the cylinder is made of
     */
    public void addCone (float x, float y, float z, float radius, float length, int segments) {
        this.addCone(x, y, z, radius, length, segments, 1F);
    }

    /**
     * Adds a cone. <p> baseScale cannot be zero. If it is, it will automatically be set to 1F.
     *
     * @param x the x-position of the base
     * @param y the y-position of the base
     * @param z the z-position of the base
     * @param radius the radius of the cylinder
     * @param length the length of the cylinder
     * @param segments the amount of segments the cylinder is made of
     * @param baseScale the scaling of the base. Can be negative.
     */
    public void addCone (float x, float y, float z, float radius, float length, int segments, float baseScale) {
        this.addCone(x, y, z, radius, length, segments, baseScale, MR_TOP);
    }

    /**
     * Adds a cone. <p> baseScale cannot be zero. If it is, it will automatically be set to 1F.
     * <p> Setting the baseDirection to either MR_LEFT, MR_BOTTOM or MR_BACK will result in the
     * top being placed at the (x,y,z).
     *
     * @param x the x-position of the base
     * @param y the y-position of the base
     * @param z the z-position of the base
     * @param radius the radius of the cylinder
     * @param length the length of the cylinder
     * @param segments the amount of segments the cylinder is made of
     * @param baseScale the scaling of the base. Can be negative.
     * @param baseDirection the direction it faces
     */
    public void addCone (float x, float y, float z, float radius, float length, int segments, float baseScale, int baseDirection) {
        this.addCone(x, y, z, radius, length, segments, baseScale, baseDirection, (int) Math.floor(radius * 2F), (int) Math.floor(radius * 2F));
    }

    /**
     * Adds a cone. <p> baseScale cannot be zero. If it is, it will automatically be set to 1F.
     * <p> Setting the baseDirection to either MR_LEFT, MR_BOTTOM or MR_BACK will result in the
     * top being placed at the (x,y,z). <p> The textures for the sides are placed next to each
     * other.
     *
     * @param x the x-position of the base
     * @param y the y-position of the base
     * @param z the z-position of the base
     * @param radius the radius of the cylinder
     * @param length the length of the cylinder
     * @param segments the amount of segments the cylinder is made of
     * @param baseScale the scaling of the base. Can be negative.
     * @param baseDirection the direction it faces
     * @param textureCircleDiameterW the diameter width of the circle on the texture
     * @param textureCircleDiameterH the diameter height of the circle on the texture
     */
    public void addCone (float x, float y, float z, float radius, float length, int segments, float baseScale, int baseDirection, int textureCircleDiameterW, int textureCircleDiameterH) {
        this.addCylinder(x, y, z, radius, length, segments, baseScale, 0.0F, baseDirection, textureCircleDiameterW, textureCircleDiameterH, 1);
    }

    /**
     * Adds a cylinder.
     *
     * @param x the x-position of the base
     * @param y the y-position of the base
     * @param z the z-position of the base
     * @param radius the radius of the cylinder
     * @param length the length of the cylinder
     * @param segments the amount of segments the cylinder is made of
     */
    public void addCylinder (float x, float y, float z, float radius, float length, int segments) {
        this.addCylinder(x, y, z, radius, length, segments, 1F, 1F);
    }

    /**
     * Adds a cylinder. <p> You can make cones by either setting baseScale or topScale to zero.
     * Setting both to zero will set the baseScale to 1F.
     *
     * @param x the x-position of the base
     * @param y the y-position of the base
     * @param z the z-position of the base
     * @param radius the radius of the cylinder
     * @param length the length of the cylinder
     * @param segments the amount of segments the cylinder is made of
     * @param baseScale the scaling of the base. Can be negative.
     * @param topScale the scaling of the top. Can be negative.
     */
    public void addCylinder (float x, float y, float z, float radius, float length, int segments, float baseScale, float topScale) {
        this.addCylinder(x, y, z, radius, length, segments, baseScale, topScale, MR_TOP);
    }

    /**
     * Adds a cylinder. <p> You can make cones by either setting baseScale or topScale to zero.
     * Setting both to zero will set the baseScale to 1F. <p> Setting the baseDirection to
     * either MR_LEFT, MR_BOTTOM or MR_BACK will result in the top being placed at the (x,y,z).
     *
     * @param x the x-position of the base
     * @param y the y-position of the base
     * @param z the z-position of the base
     * @param radius the radius of the cylinder
     * @param length the length of the cylinder
     * @param segments the amount of segments the cylinder is made of
     * @param baseScale the scaling of the base. Can be negative.
     * @param topScale the scaling of the top. Can be negative.
     * @param baseDirection the direction it faces
     */
    public void addCylinder (float x, float y, float z, float radius, float length, int segments, float baseScale, float topScale, int baseDirection) {
        this.addCylinder(x, y, z, radius, length, segments, baseScale, topScale, baseDirection, (int) Math.floor(radius * 2F), (int) Math.floor(radius * 2F), (int) Math.floor(length));
    }

    /**
     * Adds a cylinder. <p> You can make cones by either setting baseScale or topScale to zero.
     * Setting both to zero will set the baseScale to 1F. <p> Setting the baseDirection to
     * either MR_LEFT, MR_BOTTOM or MR_BACK will result in the top being placed at the (x,y,z).
     * <p> The textures for the base and top are placed next to each other, while the body will
     * be placed below the circles.
     *
     * @param x the x-position of the base
     * @param y the y-position of the base
     * @param z the z-position of the base
     * @param radius the radius of the cylinder
     * @param length the length of the cylinder
     * @param segments the amount of segments the cylinder is made of
     * @param baseScale the scaling of the base. Can be negative.
     * @param topScale the scaling of the top. Can be negative.
     * @param baseDirection the direction it faces
     * @param textureCircleDiameterW the diameter width of the circle on the texture
     * @param textureCircleDiameterH the diameter height of the circle on the texture
     * @param textureH the height of the texture of the body
     */
    public void addCylinder (float x, float y, float z, float radius, float length, int segments, float baseScale, float topScale, int baseDirection, int textureCircleDiameterW, int textureCircleDiameterH, int textureH) {
        final boolean dirTop = baseDirection == MR_TOP || baseDirection == MR_BOTTOM;
        final boolean dirSide = baseDirection == MR_RIGHT || baseDirection == MR_LEFT;
        final boolean dirFront = baseDirection == MR_FRONT || baseDirection == MR_BACK;
        final boolean dirMirror = baseDirection == MR_LEFT || baseDirection == MR_BOTTOM || baseDirection == MR_BACK;
        boolean coneBase = baseScale == 0;
        final boolean coneTop = topScale == 0;
        if (coneBase && coneTop) {
            baseScale = 1F;
            coneBase = false;
        }
        final PositionTextureVertex[] tempVerts = new PositionTextureVertex[segments * (coneBase || coneTop ? 1 : 2) + 2];
        final TexturedPolygon[] poly = new TexturedPolygon[segments * (coneBase || coneTop ? 2 : 3)];
        final float xLength = dirSide ? length : 0;
        final float yLength = dirTop ? length : 0;
        final float zLength = dirFront ? length : 0;
        final float xStart = dirMirror ? x + xLength : x;
        final float yStart = dirMirror ? y + yLength : y;
        final float zStart = dirMirror ? z + zLength : z;
        final float xEnd = !dirMirror ? x + xLength : x;
        final float yEnd = !dirMirror ? y + yLength : y;
        final float zEnd = !dirMirror ? z + zLength : z;
        tempVerts[0] = new PositionTextureVertex(xStart, yStart, zStart, 0, 0);
        tempVerts[tempVerts.length - 1] = new PositionTextureVertex(xEnd, yEnd, zEnd, 0, 0);
        float xCur = xStart;
        float yCur = yStart;
        float zCur = zStart;
        float sCur = coneBase ? topScale : baseScale;
        for (int repeat = 0; repeat < (coneBase || coneTop ? 1 : 2); repeat++) {
            for (int index = 0; index < segments; index++) {
                final float xSize = (this.mirror ^ dirMirror ? -1 : 1) * MathHelper.sin(pi / segments * index * 2F + pi) * radius * sCur;
                final float zSize = -MathHelper.cos(pi / segments * index * 2F + pi) * radius * sCur;
                final float xPlace = xCur + (!dirSide ? xSize : 0);
                final float yPlace = yCur + (!dirTop ? zSize : 0);
                final float zPlace = zCur + (dirSide ? xSize : dirTop ? zSize : 0);
                tempVerts[1 + index + repeat * segments] = new PositionTextureVertex(xPlace, yPlace, zPlace, 0, 0);
            }
            xCur = xEnd;
            yCur = yEnd;
            zCur = zEnd;
            sCur = topScale;
        }
        final float uScale = 1.0F / this.textureWidth;
        final float vScale = 1.0F / this.textureHeight;
        final float uOffset = uScale / 20.0F;
        final float vOffset = vScale / 20.0F;
        final float uCircle = textureCircleDiameterW * uScale;
        final float vCircle = textureCircleDiameterH * vScale;
        final float uWidth = (uCircle * 2F - uOffset * 2F) / segments;
        final float vHeight = textureH * vScale - uOffset * 2f;
        final float uStart = this.textureOffsetX * uScale;
        final float vStart = this.textureOffsetY * vScale;
        PositionTextureVertex[] vert;
        for (int index = 0; index < segments; index++) {
            final int index2 = (index + 1) % segments;
            final float uSize = MathHelper.sin(pi / segments * index * 2F + (!dirTop ? 0 : pi)) * (0.5F * uCircle - 2F * uOffset);
            final float vSize = MathHelper.cos(pi / segments * index * 2F + (!dirTop ? 0 : pi)) * (0.5F * vCircle - 2F * vOffset);
            final float uSize1 = MathHelper.sin(pi / segments * index2 * 2F + (!dirTop ? 0 : pi)) * (0.5F * uCircle - 2F * uOffset);
            final float vSize1 = MathHelper.cos(pi / segments * index2 * 2F + (!dirTop ? 0 : pi)) * (0.5F * vCircle - 2F * vOffset);
            vert = new PositionTextureVertex[3];
            vert[0] = tempVerts[0].setTexturePosition(uStart + 0.5F * uCircle, vStart + 0.5F * vCircle);
            vert[1] = tempVerts[1 + index2].setTexturePosition(uStart + 0.5F * uCircle + uSize1, vStart + 0.5F * vCircle + vSize1);
            vert[2] = tempVerts[1 + index].setTexturePosition(uStart + 0.5F * uCircle + uSize, vStart + 0.5F * vCircle + vSize);
            poly[index] = new TexturedPolygon(vert);
            if (this.mirror ^ this.flip)
                poly[index].flipFace();
            if (!coneBase && !coneTop) {
                vert = new PositionTextureVertex[4];
                vert[0] = tempVerts[1 + index].setTexturePosition(uStart + uOffset + uWidth * index, vStart + vOffset + vCircle);
                vert[1] = tempVerts[1 + index2].setTexturePosition(uStart + uOffset + uWidth * (index + 1), vStart + vOffset + vCircle);
                vert[2] = tempVerts[1 + segments + index2].setTexturePosition(uStart + uOffset + uWidth * (index + 1), vStart + vOffset + vCircle + vHeight);
                vert[3] = tempVerts[1 + segments + index].setTexturePosition(uStart + uOffset + uWidth * index, vStart + vOffset + vCircle + vHeight);
                poly[index + segments] = new TexturedPolygon(vert);
                if (this.mirror ^ this.flip)
                    poly[index + segments].flipFace();
            }
            vert = new PositionTextureVertex[3];
            vert[0] = tempVerts[tempVerts.length - 1].setTexturePosition(uStart + 1.5F * uCircle, vStart + 0.5F * vCircle);
            vert[1] = tempVerts[tempVerts.length - 2 - index].setTexturePosition(uStart + 1.5F * uCircle + uSize1, vStart + 0.5F * vCircle + vSize1);
            vert[2] = tempVerts[tempVerts.length - (1 + segments) + (segments - index) % segments].setTexturePosition(uStart + 1.5F * uCircle + uSize, vStart + 0.5F * vCircle + vSize);
            poly[poly.length - segments + index] = new TexturedPolygon(vert);
            if (this.mirror ^ this.flip)
                poly[poly.length - segments + index].flipFace();
        }
        this.copyTo(tempVerts, poly);
    }

    /**
     * Adds a Waveform .obj file as a model. Model files use the entire texture file.
     *
     * @param file the location of the .obj file. The location is relative to the base
     *        directories, which are either resources/models or resources/mods/models.
     */
    public void addObj (String file) {
        this.addModel(file, ModelPool.OBJ);
    }

    public void addObjF (String file) {
        try {
            this.addModelF(file, ModelPool.OBJ);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds model format support. Model files use the entire texture file.
     *
     * @param file the location of the model file. The location is relative to the base
     *        directories, which are either resources/models or resources/mods/models.
     * @param modelFormat the class of the model format interpreter
     */
    public void addModel (String file, Class modelFormat) {
        final ModelPoolEntry entry = ModelPool.addFile(file, modelFormat, this.transformGroup, this.textureGroup);
        if (entry == null)
            return;
        final PositionTextureVertex[] verts = Arrays.copyOf(entry.vertices, entry.vertices.length);
        final TexturedPolygon[] poly = Arrays.copyOf(entry.faces, entry.faces.length);
        if (this.flip)
            for (final TexturedPolygon face : this.faces)
                face.flipFace();
        this.copyTo(verts, poly, false);
    }

    public void addModelF (String file, Class modelFormat) throws IOException {
        final ModelPoolEntry entry = ModelPool.addFileF(file, modelFormat, this.transformGroup, this.textureGroup);
        if (entry == null)
            return;
        final PositionTextureVertex[] verts = Arrays.copyOf(entry.vertices, entry.vertices.length);
        final TexturedPolygon[] poly = Arrays.copyOf(entry.faces, entry.faces.length);
        if (this.flip)
            for (final TexturedPolygon face : this.faces)
                face.flipFace();
        this.copyTo(verts, poly, false);
    }

    /**
     * Sets a new position for the texture offset.
     *
     * @param x the x-coordinate of the texture start
     * @param y the y-coordinate of the texture start
     */
    @Override
    public ModelRendererTurbo setTextureOffset (int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    /**
     * Sets the position of the shape, relative to the model's origins. Note that changing the
     * offsets will not change the pivot of the model.
     *
     * @param x the x-position of the shape
     * @param y the y-position of the shape
     * @param z the z-position of the shape
     */
    public void setPosition (float x, float y, float z) {
        this.rotationPointX = x;
        this.rotationPointY = y;
        this.rotationPointZ = z;
    }

    /**
     * Mirrors the model in any direction.
     *
     * @param x whether the model should be mirrored in the x-direction
     * @param y whether the model should be mirrored in the y-direction
     * @param z whether the model should be mirrored in the z-direction
     */
    public void doMirror (boolean x, boolean y, boolean z) {
        for (final TexturedPolygon face : this.faces) {
            final PositionTextureVertex[] verts = face.vertexPositions;
            for (final PositionTextureVertex vert : verts)
                /*
                 * verts[j].vector3D.xCoord *= (x ? -1 : 1); verts[j].vector3D.yCoord *= (y ?
                 * -1 : 1); verts[j].vector3D.zCoord *= (z ? -1 : 1);
                 */
                vert.vector3D.addVector(vert.vector3D.xCoord * (x ? -1 : 1), vert.vector3D.xCoord * (y ? -1 : 1), vert.vector3D.xCoord * (z ? -1 : 1));
            if (x ^ y ^ z)
                face.flipFace();
        }
    }

    /**
     * Sets whether the shape is mirrored or not. This has effect on the way the textures get
     * displayed. When working with addSprite, addPixel and addObj, it will be ignored.
     *
     * @param isMirrored a boolean to define whether the shape is mirrored
     */
    public void setMirrored (boolean isMirrored) {
        this.mirror = isMirrored;
    }

    /**
     * Sets whether the shape's faces are flipped or not. When GL_CULL_FACE is enabled, it
     * won't render the back faces, effectively giving you the possibility to make "hollow"
     * shapes. When working with addSprite and addPixel, it will be ignored.
     *
     * @param isFlipped a boolean to define whether the shape is flipped
     */
    public void setFlipped (boolean isFlipped) {
        this.flip = isFlipped;
    }

    /**
     * Clears the current shape. Since all shapes are stacked into one shape, you can't just
     * replace a shape by overwriting the shape with another one. In this case you would need
     * to clear the shape first.
     */
    public void clear () {
        this.vertices = new PositionTextureVertex[0];
        this.faces = new TexturedPolygon[0];
        this.transformGroup.clear();
        this.transformGroup.put("0", new TransformGroupBone(new Bone(0, 0, 0, 0), 1D));
        this.currentGroup = this.transformGroup.get("0");
    }

    /**
     * Copies an array of vertices and polygons to the current shape. This mainly is used to
     * copy each shape to the main class, but you can just use it to copy your own shapes, for
     * example from other classes, into the current class.
     *
     * @param verts the array of vertices you want to copy
     * @param poly the array of polygons you want to copy
     */
    public void copyTo (PositionTextureVertex[] verts, TexturedPolygon[] poly) {
        this.copyTo(verts, poly, true);
    }

    public void copyTo (PositionTextureVertex[] verts, TexturedPolygon[] poly, boolean copyGroup) {
        this.vertices = Arrays.copyOf(this.vertices, this.vertices.length + verts.length);
        this.faces = Arrays.copyOf(this.faces, this.faces.length + poly.length);
        for (int idx = 0; idx < verts.length; idx++) {
            this.vertices[this.vertices.length - verts.length + idx] = verts[idx];
            if (copyGroup && verts[idx] instanceof PositionTransformVertex)
                ((PositionTransformVertex) verts[idx]).addGroup(this.currentGroup);
        }
        for (int idx = 0; idx < poly.length; idx++) {
            this.faces[this.faces.length - poly.length + idx] = poly[idx];
            if (copyGroup)
                this.currentTextureGroup.addPoly(poly[idx]);
        }
    }

    /**
     * Copies an array of vertices and quads to the current shape. This method converts quads
     * to polygons and then calls the main copyTo method.
     *
     * @param verts the array of vertices you want to copy
     * @param quad the array of quads you want to copy
     */
    public void copyTo (PositionTextureVertex[] verts, TexturedQuad[] quad) {
        final TexturedPolygon[] poly = new TexturedPolygon[quad.length];
        for (int idx = 0; idx < quad.length; idx++)
            poly[idx] = new TexturedPolygon(quad[idx].vertexPositions);
        this.copyTo(verts, poly);
    }

    /**
     * Sets the current transformation group. The transformation group is used to allow for
     * vertex transformation. If a transformation group does not exist, a new one will be
     * created.
     *
     * @param groupName the name of the transformation group you want to switch to
     * @param bone the Bone this transformation group is attached to
     * @param weight the weight of the transformation group
     */
    public void setGroup (String groupName, Bone bone, double weight) {
        if (!this.transformGroup.containsKey(groupName))
            this.transformGroup.put(groupName, new TransformGroupBone(bone, weight));
        this.currentGroup = this.transformGroup.get(groupName);
    }

    /**
     * Gets the current transformation group.
     *
     * @return the current PositionTransformGroup.
     */
    public TransformGroup getGroup () {
        return this.currentGroup;
    }

    /**
     * Sets the current transformation group. The transformation group is used to allow for
     * vertex transformation. If a transformation group does not exist, a new one will be
     * created.
     *
     * @param groupName the name of the transformation group you want to switch to
     */
    public void setGroup (String groupName) {
        this.setGroup(groupName, new Bone(0, 0, 0, 0), 1D);
    }

    /**
     * Gets the transformation group with a given group name.
     *
     * @return the current PositionTransformGroup.
     */
    public TransformGroup getGroup (String groupName) {
        if (!this.transformGroup.containsKey(groupName))
            return null;
        return this.transformGroup.get(groupName);
    }

    /**
     * Gets the current texture group.
     *
     * @return a TextureGroup object.
     */
    public TextureGroup getTextureGroup () {
        return this.currentTextureGroup;
    }

    /**
     * Sets the current texture group, which is used to switch the textures on a per-model
     * base. Do note that any model that is rendered afterwards will use the same texture. To
     * counter it, set a default texture, either at initialization or before rendering.
     *
     * @param groupName The name of the texture group. If the texture group doesn't exist, it
     *        creates a new group automatically.
     */
    public void setTextureGroup (String groupName) {
        if (!this.textureGroup.containsKey(groupName))
            this.textureGroup.put(groupName, new TextureGroup());
        this.currentTextureGroup = this.textureGroup.get(groupName);
    }

    /**
     * Gets the texture group with the given name.
     *
     * @param groupName the name of the texture group to return
     *
     * @return a TextureGroup object.
     */
    public TextureGroup getTextureGroup (String groupName) {
        if (!this.textureGroup.containsKey(groupName))
            return null;
        return this.textureGroup.get(groupName);
    }

    /**
     * Sets the texture of the current texture group.
     *
     * @param s the filename
     */
    public void setGroupTexture (String s) {
        this.currentTextureGroup.texture = s;
    }

    /**
     * Sets the default texture. When left as an empty string, it will use the texture that has
     * been set previously. Note that this will also move on to other rendered models of the
     * same entity.
     *
     * @param s the filename
     */
    public void setDefaultTexture (String s) {
        this.defaultTexture = s;
    }

    public void render () {
        this.render(0.0625F);
    }

    /**
     * Renders the shape.
     *
     * @param worldScale the scale of the shape. Usually is 0.0625.
     */
    @Override
    public void render (float worldScale) {
        if (this.field_1402_i)
            return;
        if (!this.showModel)
            return;
        if (!this.compiled || this.forcedRecompile)
            this.compileDisplayList(worldScale);
        if (this.rotateAngleX != 0.0F || this.rotateAngleY != 0.0F || this.rotateAngleZ != 0.0F) {
            GL11.glPushMatrix();
            GL11.glTranslatef(this.rotationPointX * worldScale, this.rotationPointY * worldScale, this.rotationPointZ * worldScale);
            if (this.rotateAngleZ != 0.0F)
                GL11.glRotatef(this.rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
            if (this.rotateAngleY != 0.0F)
                GL11.glRotatef(this.rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
            if (this.rotateAngleX != 0.0F)
                GL11.glRotatef(this.rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
            this.callDisplayList();
            if (this.childModels != null)
                for (int i = 0; i < this.childModels.size(); i++)
                    ((ModelRenderer) this.childModels.get(i)).render(worldScale);
            GL11.glPopMatrix();
        }
        else if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
            GL11.glTranslatef(this.rotationPointX * worldScale, this.rotationPointY * worldScale, this.rotationPointZ * worldScale);
            this.callDisplayList();
            if (this.childModels != null)
                for (int i = 0; i < this.childModels.size(); i++)
                    ((ModelRenderer) this.childModels.get(i)).render(worldScale);
            GL11.glTranslatef(-this.rotationPointX * worldScale, -this.rotationPointY * worldScale, -this.rotationPointZ * worldScale);
        }
        else {
            this.callDisplayList();
            if (this.childModels != null)
                for (int i = 0; i < this.childModels.size(); i++)
                    ((ModelRenderer) this.childModels.get(i)).render(worldScale);
        }
    }

    @Override
    public void renderWithRotation (float f) {
        if (this.field_1402_i)
            return;
        if (!this.showModel)
            return;
        if (!this.compiled)
            this.compileDisplayList(f);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.rotationPointX * f, this.rotationPointY * f, this.rotationPointZ * f);
        if (this.rotateAngleY != 0.0F)
            GL11.glRotatef(this.rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
        if (this.rotateAngleX != 0.0F)
            GL11.glRotatef(this.rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
        if (this.rotateAngleZ != 0.0F)
            GL11.glRotatef(this.rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
        this.callDisplayList();
        GL11.glPopMatrix();
    }

    @Override
    public void postRender (float f) {
        if (this.field_1402_i)
            return;
        if (!this.showModel)
            return;
        if (!this.compiled || this.forcedRecompile)
            this.compileDisplayList(f);
        if (this.rotateAngleX != 0.0F || this.rotateAngleY != 0.0F || this.rotateAngleZ != 0.0F) {
            GL11.glTranslatef(this.rotationPointX * f, this.rotationPointY * f, this.rotationPointZ * f);
            if (this.rotateAngleZ != 0.0F)
                GL11.glRotatef(this.rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
            if (this.rotateAngleY != 0.0F)
                GL11.glRotatef(this.rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
            if (this.rotateAngleX != 0.0F)
                GL11.glRotatef(this.rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
        }
        else if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F)
            GL11.glTranslatef(this.rotationPointX * f, this.rotationPointY * f, this.rotationPointZ * f);
    }

    private void callDisplayList () {
        if (this.useLegacyCompiler)
            GL11.glCallList(this.displayList);
        else {
            final TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
            final Collection<TextureGroup> textures = this.textureGroup.values();
            final Iterator<TextureGroup> itr = textures.iterator();
            for (int i = 0; itr.hasNext(); i++) {
                final TextureGroup curTexGroup = itr.next();
                curTexGroup.loadTexture();
                GL11.glCallList(this.displayListArray[i]);
                if (!this.defaultTexture.equals(""))
                    renderEngine.bindTexture(new ResourceLocation("", this.defaultTexture));
            }
        }
    }

    private void compileDisplayList (float worldScale) {
        if (this.useLegacyCompiler)
            this.compileLegacyDisplayList(worldScale);
        else {
            final Collection<TextureGroup> textures = this.textureGroup.values();
            final Iterator<TextureGroup> itr = textures.iterator();
            this.displayListArray = new int[this.textureGroup.size()];
            for (int i = 0; itr.hasNext(); i++) {
                this.displayListArray[i] = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(this.displayListArray[i], 4864);
                final Tessellator tessellator = Tessellator.getInstance();
                final TextureGroup usedGroup = itr.next();
                for (int j = 0; j < usedGroup.poly.size(); j++)
                    usedGroup.poly.get(j).draw(tessellator, worldScale);
                GL11.glEndList();
            }
        }
        this.compiled = true;
    }

    private void compileLegacyDisplayList (float worldScale) {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(this.displayList, 4864 /* GL_COMPILE */);
        final Tessellator tessellator = Tessellator.getInstance();
        for (final TexturedPolygon face : this.faces)
            face.draw(tessellator, worldScale);
        GL11.glEndList();
    }

    public void addShapeBox (float x, float y, float z, int w, int h, int d, float scale, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float x5, float y5, float z5, float x6, float y6, float z6, float x7, float y7, float z7) {
        float f4 = x + w, f5 = y + h, f6 = z + d;
        x -= scale;
        y -= scale;
        z -= scale;
        f4 += scale;
        f5 += scale;
        f6 += scale;
        if (this.mirror) {
            final float f7 = f4;
            f4 = x;
            x = f7;
        }
        final float[] v = { x - x0, y - y0, z - z0 }, v1 = { f4 + x1, y - y1, z - z1 }, v2 = { f4 + x5, f5 + y5, z - z5 };
        final float[] v3 = { x - x4, f5 + y4, z - z4 }, v4 = { x - x3, y - y3, f6 + z3 }, v5 = { f4 + x2, y - y2, f6 + z2 };
        final float[] v6 = { f4 + x6, f5 + y6, f6 + z6 }, v7 = { x - x7, f5 + y7, f6 + z7 };
        this.addRectShape(v, v1, v2, v3, v4, v5, v6, v7, w, h, d);
    }
}
