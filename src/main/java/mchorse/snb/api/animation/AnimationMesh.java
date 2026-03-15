package mchorse.snb.api.animation;

import mchorse.mclib.client.render.VertexBuilder;
import mchorse.mclib.utils.MathUtils;
import mchorse.snb.api.bobj.BOBJArmature;
import mchorse.snb.api.bobj.BOBJBone;
import mchorse.snb.api.bobj.BOBJLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Animation mesh class
 * 
 * This class is responsible for managing an animated mesh. It's 
 * includes binding its texture, animating the vertices based on 
 * armature and weight data, and managing OpengGL resources associated 
 * with this mesh.
 */
@SideOnly(Side.CLIENT)
public class AnimationMesh
{
    public static final boolean DEBUG = false;

    /**
     * Owner of this mesh 
     */
    public Animation owner;

    /**
     * Texture which is going binded when playing back
     */
    public ResourceLocation texture;

    /**
     * Name of this mesh 
     */
    public String name;

    /**
     * Compiled data which has all needed information about the mesh 
     */
    public BOBJLoader.CompiledData data;

    /**
     * Local reference for armature 
     */
    public BOBJArmature armature;

    /**
     * Color alpha
     */
    public float alpha = 1F;

    /* Sharp bending joints */
    public Joint armLeft;
    public Joint armRight;
    public Joint legLeft;
    public Joint legRight;
    public Joint body;

    /**
     * Current mesh configuration
     */
    private AnimationMeshConfig currentConfig;

    /* Buffers */
    public FloatBuffer vertices;
    public FloatBuffer normals;
    public FloatBuffer tangents;
    public FloatBuffer textcoords;
    public IntBuffer indices;

    /* GL buffers */
    public int vertexBuffer;
    public int normalBuffer;
    public int tangentBuffer;
    public int texcoordBuffer;
    public int indexBuffer;

    public AnimationMesh(Animation owner, String name, BOBJLoader.CompiledData data)
    {
        this.owner = owner;
        this.name = name;
        this.data = data;
        this.armature = this.data.mesh.armature;
        this.armature.initArmature();

        this.initBuffers();
        this.initJoints();
    }

    /**
     * Initialize sharp bending joints
     */
    private void initJoints()
    {
        BOBJBone leftArm = this.armature.bones.get("left_arm");
        BOBJBone lowLeftArm = this.armature.bones.get("low_left_arm");
        BOBJBone rightArm = this.armature.bones.get("right_arm");
        BOBJBone lowRightArm = this.armature.bones.get("low_right_arm");
        BOBJBone leftLeg = this.armature.bones.get("left_leg");
        BOBJBone lowLeftLeg = this.armature.bones.get("low_left_leg");
        BOBJBone rightLeg = this.armature.bones.get("right_leg");
        BOBJBone lowRightLeg = this.armature.bones.get("low_leg_right");
        BOBJBone bodyBone = this.armature.bones.get("body");
        BOBJBone lowBody = this.armature.bones.get("low_body");

        if (leftArm != null && lowLeftArm != null)
        {
            this.armLeft = new Joint(leftArm, lowLeftArm);
        }
        if (rightArm != null && lowRightArm != null)
        {
            this.armRight = new Joint(rightArm, lowRightArm);
        }
        if (leftLeg != null && lowLeftLeg != null)
        {
            this.legLeft = new Joint(leftLeg, lowLeftLeg);
        }
        if (rightLeg != null && lowRightLeg != null)
        {
            this.legRight = new Joint(rightLeg, lowRightLeg);
        }
        if (bodyBone != null && lowBody != null)
        {
            this.body = new Joint(bodyBone, lowBody);
        }
    }

    /**
     * Initiate buffers. This method is responsible for allocating 
     * buffers for the data to be passed to VBOs and also generating the 
     * VBOs themselves. 
     */
    private void initBuffers()
    {
        this.vertices = BufferUtils.createFloatBuffer(this.data.posData.length);
        this.vertices.put(this.data.posData).flip();

        this.normals = BufferUtils.createFloatBuffer(this.data.normData.length);
        this.normals.put(this.data.normData).flip();

        this.tangents = BufferUtils.createFloatBuffer(this.data.posData.length);

        this.textcoords = BufferUtils.createFloatBuffer(this.data.texData.length);
        this.textcoords.put(this.data.texData).flip();

        this.indices = BufferUtils.createIntBuffer(this.data.indexData.length);
        this.indices.put(this.data.indexData).flip();

        this.vertexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vertices, GL15.GL_DYNAMIC_DRAW);

        this.normalBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.normalBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.normals, GL15.GL_STATIC_DRAW);

        this.tangentBuffer = GL15.glGenBuffers();

        this.texcoordBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.texcoordBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.textcoords, GL15.GL_STATIC_DRAW);

        this.indexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indices, GL15.GL_STATIC_DRAW);

        /* Unbind the buffer. REQUIRED to avoid OpenGL crash */
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Set texture filtering
     */
    public void setFiltering(int filtering)
    {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filtering);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filtering);
    }

    /**
     * Get texture filtering (not really used)
     */
    public int getFiltering()
    {
        return GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
    }

    /**
     * Clean up resources which were used by this  
     */
    public void delete()
    {
        GL15.glDeleteBuffers(this.vertexBuffer);
        GL15.glDeleteBuffers(this.normalBuffer);
        GL15.glDeleteBuffers(this.texcoordBuffer);
        GL15.glDeleteBuffers(this.indexBuffer);

        this.vertices = null;
        this.normals = null;
        this.textcoords = null;
        this.indices = null;
    }

    /**
     * Update this mesh. This method is responsible for applying 
     * matrix transformations to vertices and normals according to its 
     * bone owners and these bone influences.
     */
    public void updateMesh()
    {
        this.updateMesh(this.currentConfig);
    }

    /**
     * Update this mesh with a specific config
     */
    public void updateMesh(AnimationMeshConfig config)
    {
        int max = this.data.maxWeights;

        Vector4f sumVertex = new Vector4f();
        Vector4f resultVertex = new Vector4f(0, 0, 0, 0);

        Vector3f sumNormal = new Vector3f();
        Vector3f resultNormal = new Vector3f(0, 0, 0);

        float[] oldVertices = this.data.posData;
        float[] newVertices = new float[oldVertices.length];

        float[] oldNormals = this.data.normData;
        float[] newNormals = new float[oldNormals.length];

        Matrix4f[] matrices = this.armature.matrices;

        for (int i = 0, c = newVertices.length / 4; i < c; i++)
        {
            int count = 0;

            for (int w = 0; w < max; w++)
            {
                float weight = this.data.weightData[i * max + w];

                if (weight > 0)
                {
                    int index = this.data.boneIndexData[i * max + w];

                    sumVertex.set(oldVertices[i * 4], oldVertices[i * 4 + 1], oldVertices[i * 4 + 2], oldVertices[i * 4 + 3]);
                    matrices[index].transform(sumVertex);
                    sumVertex.scale(weight);
                    resultVertex.add(sumVertex);

                    sumNormal.set(oldNormals[i * 3], oldNormals[i * 3 + 1], oldNormals[i * 3 + 2]);
                    matrices[index].transform(sumNormal);
                    sumNormal.scale(weight);
                    resultNormal.add(sumNormal);

                    count++;
                }
            }

            if (count == 0)
            {
                resultNormal.set(oldNormals[i * 3], oldNormals[i * 3 + 1], oldNormals[i * 3 + 2]);
                resultVertex.set(oldVertices[i * 4], oldVertices[i * 4 + 1], oldVertices[i * 4 + 2], 1);
            }

            /* Thanks MiaoNLI for the fix insight! */
            resultVertex.x /= resultVertex.w;
            resultVertex.y /= resultVertex.w;
            resultVertex.z /= resultVertex.w;

            newVertices[i * 4] = resultVertex.x;
            newVertices[i * 4 + 1] = resultVertex.y;
            newVertices[i * 4 + 2] = resultVertex.z;
            newVertices[i * 4 + 3] = 1;

            newNormals[i * 3] = resultNormal.x;
            newNormals[i * 3 + 1] = resultNormal.y;
            newNormals[i * 3 + 2] = resultNormal.z;

            resultVertex.set(0, 0, 0, 0);
            resultNormal.set(0, 0, 0);
        }

        // Apply sharp bending if enabled
        if (config != null && config.sharpBending)
        {
            this.processSharpBending(newVertices, newNormals);
        }

        this.updateVertices(newVertices);
        this.updateNormals(newNormals);
        this.updateTangent(newVertices, newNormals);
    }

    /**
     * Process sharp bending for joints
     */
    private void processSharpBending(float[] newVertices, float[] newNormals)
    {
        if (this.armLeft != null && !this.armLeft.isFilled())
        {
            float rmn1 = 22 / 64F;
            float rmx1 = 30 / 64F;
            float rmn2 = 54 / 64F;
            float rmx2 = 62 / 64F;
            float rmn3 = 38 / 64F;
            float rmx3 = 46 / 64F;

            for (int i = 0, c = this.data.posData.length / 4; i < c; i++)
            {
                double v = this.data.texData[i * 2 + 1];
                JointType type = JointType.NONE;

                for (int j = 0; j < this.data.maxWeights; j++)
                {
                    int boneIndex = this.data.boneIndexData[i * this.data.maxWeights + j];

                    if (boneIndex == -1)
                    {
                        continue;
                    }

                    BOBJBone bone = this.armature.orderedBones.get(boneIndex);

                    if (bone.name.contains("leg"))
                    {
                        type = JointType.LEG;
                    }
                    else if (bone.name.contains("arm"))
                    {
                        type = JointType.ARM;
                    }
                    else if (bone.name.contains("body"))
                    {
                        type = JointType.BODY;
                    }

                    if (type != JointType.NONE)
                    {
                        break;
                    }
                }

                if (((v >= rmn1 && v <= rmx1) || (v >= rmn2 && v <= rmx2) || (v >= rmn3 && v <= rmx3)) && type != JointType.NONE)
                {
                    float z = this.data.posData[i * 4 + 2];
                    Joint joint;

                    if (type == JointType.BODY)
                    {
                        joint = this.body;
                    }
                    else if (v > 3 / 4F)
                    {
                        joint = type == JointType.LEG ? this.legLeft : this.armLeft;
                    }
                    else
                    {
                        joint = type == JointType.LEG ? this.legRight : this.armRight;
                    }

                    if (joint != null)
                    {
                        List<Integer> list = z < 0 ? joint.back : joint.front;
                        list.add(i);
                    }
                }
            }
        }

        if (this.armRight != null) this.armRight.process(this.data, this.armature, newVertices, newNormals);
        if (this.armLeft != null) this.armLeft.process(this.data, this.armature, newVertices, newNormals);
        if (this.legRight != null) this.legRight.process(this.data, this.armature, newVertices, newNormals);
        if (this.legLeft != null) this.legLeft.process(this.data, this.armature, newVertices, newNormals);
        if (this.body != null) this.body.process(this.data, this.armature, newVertices, newNormals);
    }

    /**
     * Update mesh with given data 
     */
    public void updateVertices(float[] data)
    {
        this.vertices.clear();
        this.vertices.put(data).flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vertices, GL15.GL_DYNAMIC_DRAW);
    }

    /**
     * Update mesh with given data 
     */
    public void updateNormals(float[] data)
    {
        this.normals.clear();
        this.normals.put(data).flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.normalBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.normals, GL15.GL_DYNAMIC_DRAW);
    }

    /**
     * Update mesh with given data (Optifine)
     */
    public void updateTangent(float[] newVertices, float[] newNormals)
    {
        float[] newTangents = new float[newVertices.length];
        boolean[] updated = new boolean[newVertices.length];

        for (int i = 0; i < this.data.indexData.length / 3; i++)
        {
            int i0 = this.data.indexData[i * 3];
            int i1 = this.data.indexData[i * 3 + 1];
            int i2 = this.data.indexData[i * 3 + 2];

            if (updated[i0])
            {
                newTangents[i1 * 4] = newTangents[i2 * 4] = newTangents[i0 * 4];
                newTangents[i1 * 4 + 1] = newTangents[i2 * 4 + 1] = newTangents[i0 * 4 + 1];
                newTangents[i1 * 4 + 2] = newTangents[i2 * 4 + 2] = newTangents[i0 * 4 + 2];
                newTangents[i1 * 4 + 3] = newTangents[i2 * 4 + 3] = newTangents[i0 * 4 + 3];
            }
            else if (updated[i1])
            {
                newTangents[i0 * 4] = newTangents[i2 * 4] = newTangents[i1 * 4];
                newTangents[i0 * 4 + 1] = newTangents[i2 * 4 + 1] = newTangents[i1 * 4 + 1];
                newTangents[i0 * 4 + 2] = newTangents[i2 * 4 + 2] = newTangents[i1 * 4 + 2];
                newTangents[i0 * 4 + 3] = newTangents[i2 * 4 + 3] = newTangents[i1 * 4 + 3];
            }
            else if (updated[i2])
            {
                newTangents[i0 * 4] = newTangents[i1 * 4] = newTangents[i2 * 4];
                newTangents[i0 * 4 + 1] = newTangents[i1 * 4 + 1] = newTangents[i2 * 4 + 1];
                newTangents[i0 * 4 + 2] = newTangents[i1 * 4 + 2] = newTangents[i2 * 4 + 2];
                newTangents[i0 * 4 + 3] = newTangents[i1 * 4 + 3] = newTangents[i2 * 4 + 3];
            }
            else
            {
                Point3f[] vertices = new Point3f[3];
                Point2f[] uvs = new Point2f[3];
                Vector3f normal = new Vector3f();

                vertices[0] = new Point3f(newVertices[i0 * 4], newVertices[i0 * 4 + 1], newVertices[i0 * 4 + 2]);
                vertices[1] = new Point3f(newVertices[i1 * 4], newVertices[i1 * 4 + 1], newVertices[i1 * 4 + 2]);
                vertices[2] = new Point3f(newVertices[i2 * 4], newVertices[i2 * 4 + 1], newVertices[i2 * 4 + 2]);

                uvs[0] = new Point2f((float) this.data.texData[i0 * 2], (float) this.data.texData[i0 * 2 + 1]);
                uvs[1] = new Point2f((float) this.data.texData[i1 * 2], (float) this.data.texData[i1 * 2 + 1]);
                uvs[2] = new Point2f((float) this.data.texData[i2 * 2], (float) this.data.texData[i2 * 2 + 1]);

                normal = new Vector3f(newNormals[i0 * 3], newNormals[i0 * 3 + 1], newNormals[i0 * 3 + 2]);

                Vector4f tangent = VertexBuilder.calcTangent(vertices, uvs, normal);

                newTangents[i0 * 4] = newTangents[i1 * 4] = newTangents[i2 * 4] = tangent.x * 32767F;
                newTangents[i0 * 4 + 1] = newTangents[i1 * 4 + 1] = newTangents[i2 * 4 + 1] = tangent.y * 32767F;
                newTangents[i0 * 4 + 2] = newTangents[i1 * 4 + 2] = newTangents[i2 * 4 + 2] = tangent.z * 32767F;
                newTangents[i0 * 4 + 3] = newTangents[i1 * 4 + 3] = newTangents[i2 * 4 + 3] = tangent.w * 32767F;

                updated[i0] = updated[i1] = updated[i2] = true;
            }
        }

        this.tangents.clear();
        this.tangents.put(newTangents).flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.tangentBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.tangents, GL15.GL_DYNAMIC_DRAW);
    }

    /**
     * Just renders this mesh with whatever data it has
     */
    public void render(Minecraft mc, AnimationMeshConfig config)
    {
        this.currentConfig = config;
        
        if (config != null && (!config.visible || this.alpha <= 0))
        {
            return;
        }

        ResourceLocation texture = this.getTexture(config);
        boolean smooth = config == null ? false : config.smooth;
        boolean normals = config == null ? false : config.normals;
        boolean lighting = config == null ? true : config.lighting;

        float lastX = OpenGlHelper.lastBrightnessX;
        float lastY = OpenGlHelper.lastBrightnessY;

        /* Bind the texture */
        if (texture != null)
        {
            mc.renderEngine.bindTexture(texture);

            if (config != null)
            {
                this.setFiltering(config.filtering);
            }
        }

        if (smooth && normals) GL11.glShadeModel(GL11.GL_SMOOTH);
        if (!normals) RenderHelper.disableStandardItemLighting();
        if (!lighting) OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

        int color = config != null ? config.color : 0xffffff;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float a = this.alpha;

        GlStateManager.color(r, g, b, a);

        /* Bind vertex array */
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL11.glVertexPointer(4, GL11.GL_FLOAT, 0, 0);

        /* Bind normal array */
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.normalBuffer);
        GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);

        /* Bind UV array */
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.texcoordBuffer);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        /* Bind tangent array */
        if (VertexBuilder.tangentAttrib != -1)
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.tangentBuffer);
            GL20.glVertexAttribPointer(VertexBuilder.tangentAttrib, 4, GL11.GL_FLOAT, false, 0, 0);
            GL20.glEnableVertexAttribArray(VertexBuilder.tangentAttrib);
        }

        /* Render with index buffer */
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);
        GL11.glDrawElements(GL11.GL_TRIANGLES, this.data.indexData.length, GL11.GL_UNSIGNED_INT, 0);

        /* Unbind the buffer. REQUIRED to avoid OpenGL crash */
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        if (VertexBuilder.tangentAttrib != -1)
        {
            GL20.glDisableVertexAttribArray(VertexBuilder.tangentAttrib);
        }

        if (smooth && normals) GL11.glShadeModel(GL11.GL_FLAT);
        if (!normals) RenderHelper.enableStandardItemLighting();
        if (!lighting) OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);

        /* Rendering skeletal debug information */
        if (mc.gameSettings.showDebugInfo && !mc.gameSettings.hideGUI && DEBUG)
        {
            /* Skeletal information shouldn't be affected by lighting, 
             * depth (i.e. it will render a top) or textures */
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();

            for (BOBJBone bone : this.data.mesh.armature.orderedBones)
            {
                Vector4f vec1 = new Vector4f(0, 0, 0, 1);
                Vector4f vec2 = new Vector4f(0, bone.length, 0, 1);
                Vector4f x = new Vector4f(0.1F, 0, 0, 1);
                Vector4f y = new Vector4f(0, 0.1F, 0, 1);
                Vector4f z = new Vector4f(0, 0, 0.1F, 1);
                Matrix4f mat = bone.mat;

                mat.transform(vec1);
                mat.transform(vec2);
                mat.transform(x);
                mat.transform(y);
                mat.transform(z);

                /* Draw a point of bone's head */
                GL11.glPointSize(5);
                GL11.glBegin(GL11.GL_POINTS);
                GlStateManager.color(1, 1, 1);
                GL11.glVertex3f(vec1.x, vec1.y, vec1.z);
                GL11.glEnd();

                /* Draw a line between bone's head and tail */
                GL11.glLineWidth(1);
                GL11.glBegin(GL11.GL_LINES);
                GlStateManager.color(0.9F, 0.9F, 0.9F);
                GL11.glVertex3f(vec1.x, vec1.y, vec1.z);
                GL11.glVertex3f(vec2.x, vec2.y, vec2.z);
                GL11.glEnd();

                /* Draw head's axes */
                GL11.glLineWidth(2);
                GL11.glBegin(GL11.GL_LINES);
                GlStateManager.color(1, 0, 0);
                GL11.glVertex3f(vec1.x, vec1.y, vec1.z);
                GL11.glVertex3f(x.x, x.y, x.z);
                GL11.glEnd();

                GL11.glBegin(GL11.GL_LINES);
                GlStateManager.color(0, 1, 0);
                GL11.glVertex3f(vec1.x, vec1.y, vec1.z);
                GL11.glVertex3f(y.x, y.y, y.z);
                GL11.glEnd();

                GL11.glBegin(GL11.GL_LINES);
                GlStateManager.color(0, 0, 1);
                GL11.glVertex3f(vec1.x, vec1.y, vec1.z);
                GL11.glVertex3f(z.x, z.y, z.z);
                GL11.glEnd();

                GlStateManager.color(1, 1, 1);
                GL11.glLineWidth(1);
            }

            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }

        this.alpha = 1;
    }

    /**
     * Get resource location based on the passed config 
     */
    private ResourceLocation getTexture(AnimationMeshConfig config)
    {
        if (config == null)
        {
            return this.texture;
        }

        return config.texture == null ? this.texture : config.texture;
    }

    /**
     * Joint class for sharp bending
     */
    public static class Joint
    {
        public static Vector4f temporary = new Vector4f();

        public List<Integer> front = new ArrayList<Integer>();
        public List<Integer> back = new ArrayList<Integer>();
        public BOBJBone top;
        public BOBJBone joint;

        public Joint(BOBJBone top, BOBJBone joint)
        {
            this.top = top;
            this.joint = joint;
        }

        public boolean isFilled()
        {
            return !this.front.isEmpty();
        }

        public void process(BOBJLoader.CompiledData data, BOBJArmature armature, float[] posData, float[] normalData)
        {
            final float pi = (float) Math.PI;

            float rotation = this.joint.rotateX;
            float frontFactor = MathUtils.clamp((rotation + pi / 2F) / pi, 0, 1);
            float backFactor = 1 - frontFactor;

            this.processSide(data, armature, this.front, posData, normalData, frontFactor);
            this.processSide(data, armature, this.back, posData, normalData, backFactor);
        }

        protected void processSide(BOBJLoader.CompiledData data, BOBJArmature armature, List<Integer> indices, float[] posData, float[] normalData, float factor)
        {
            int prevIndex = 0;

            for (int i : indices)
            {
                float x = data.posData[i * 4];
                float y = data.posData[i * 4 + 1] + factor * 4 / 16F - 2 / 16F;
                float z = data.posData[i * 4 + 2];

                temporary.set(x, y, z, 1);
                armature.matrices[this.top.index].transform(temporary);

                posData[i * 4] = temporary.x;
                posData[i * 4 + 1] = temporary.y;
                posData[i * 4 + 2] = temporary.z;
                posData[i * 4 + 3] = temporary.w;

                /* Copying the normal from the third/second side */
                int base = i - i % 3;
                int a = i - base;
                int b = prevIndex - base;
                int c = 0;

                if (b >= 0)
                {
                    /* If previous normal is from the same triangle, there is a need
                     * to figure out what is the third vertex is */
                    if ((a == 0 && b == 2) || (b == 0 && a == 2))
                    {
                        c = 1;
                    }
                    else if ((a == 0 && b == 1) || (b == 0 && a == 1))
                    {
                        c = 2;
                    }
                }
                else
                {
                    /* If there is only one transformed sharpened joint, then we
                     * can take just any as long as it's not the same as current index */
                    c = a == 1 ? 0 : 1;
                }

                c += base;

                normalData[i * 3] = normalData[c * 3];
                normalData[i * 3 + 1] = normalData[c * 3 + 1];
                normalData[i * 3 + 2] = normalData[c * 3 + 2];

                if (b >= 0)
                {
                    normalData[prevIndex * 3] = normalData[c * 3];
                    normalData[prevIndex * 3 + 1] = normalData[c * 3 + 1];
                    normalData[prevIndex * 3 + 2] = normalData[c * 3 + 2];
                }

                prevIndex = i;
            }
        }
    }

    public enum JointType
    {
        LEG, ARM, BODY, NONE
    }
}