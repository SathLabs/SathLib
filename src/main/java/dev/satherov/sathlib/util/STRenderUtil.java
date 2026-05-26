package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix4f;

@UtilityClass
@SuppressWarnings("DuplicatedCode")
public class STRenderUtil {
    
    // ==================== Line Box Drawing ====================
    
    public static void drawLineBox(VertexConsumer buffer, Matrix4f matrix, AABB box, int rgba, float width) {
        STRenderUtil.drawLineBox(buffer, matrix,
                (float) box.minX, (float) box.minY, (float) box.minZ,
                (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                rgba, width
        );
    }
    
    public static void drawLineBox(VertexConsumer buffer, Matrix4f matrix, BoundingBox box, int rgba, float width) {
        STRenderUtil.drawLineBox(buffer, matrix,
                box.minX(), box.minY(), box.minZ(),
                box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1,
                rgba, width
        );
    }
    
    public static void drawLineBox(VertexConsumer buffer, Matrix4f matrix, BlockPos pos, int rgba, float width) {
        STRenderUtil.drawLineBox(buffer, matrix,
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                rgba, width
        );
    }
    
    public static void drawLineBox(VertexConsumer buffer, Matrix4f matrix, BlockPos min, BlockPos max, int rgba, float width) {
        STRenderUtil.drawLineBox(buffer, matrix,
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1, max.getY() + 1, max.getZ() + 1,
                rgba, width
        );
    }
    
    public static void drawLineBox(VertexConsumer buffer, Matrix4f matrix, Vec3 min, Vec3 max, int rgba, float width) {
        STRenderUtil.drawLineBox(buffer, matrix,
                (float) min.x(), (float) min.y(), (float) min.z(),
                (float) max.x(), (float) max.y(), (float) max.z(),
                rgba, width
        );
    }
    
    public static void drawLineBox(VertexConsumer buffer, Matrix4f matrix, Vec3i min, Vec3i max, int rgba, float width) {
        STRenderUtil.drawLineBox(buffer, matrix,
                min.getX(), min.getY(), min.getZ(),
                max.getX(), max.getY(), max.getZ(),
                rgba, width
        );
    }
    
    public static void drawLineBox(VertexConsumer buffer, Matrix4f matrix,
                                   float minX, float minY, float minZ,
                                   float maxX, float maxY, float maxZ,
                                   int rgba, float width
    ) {
        // Bottom
        STRenderUtil.drawLine(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, rgba, width);
        
        // Top
        STRenderUtil.drawLine(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, rgba, width);
        
        // Vertical
        STRenderUtil.drawLine(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, rgba, width);
        STRenderUtil.drawLine(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, rgba, width);
    }
    
    // ==================== Filled Box Drawing ====================
    
    public static void drawFilledBox(VertexConsumer buffer, Matrix4f matrix, AABB box, int rgba) {
        STRenderUtil.drawFilledBox(buffer, matrix,
                (float) box.minX, (float) box.minY, (float) box.minZ,
                (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                rgba
        );
    }
    
    public static void drawFilledBox(VertexConsumer buffer, Matrix4f matrix, BoundingBox box, int rgba) {
        STRenderUtil.drawFilledBox(buffer, matrix,
                box.minX(), box.minY(), box.minZ(),
                box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1,
                rgba
        );
    }
    
    public static void drawFilledBox(VertexConsumer buffer, Matrix4f matrix, BlockPos pos, int rgba) {
        STRenderUtil.drawFilledBox(buffer, matrix,
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                rgba
        );
    }
    
    public static void drawFilledBox(VertexConsumer buffer, Matrix4f matrix, BlockPos min, BlockPos max, int rgba) {
        STRenderUtil.drawFilledBox(buffer, matrix,
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1, max.getY() + 1, max.getZ() + 1,
                rgba
        );
    }
    
    public static void drawFilledBox(VertexConsumer buffer, Matrix4f matrix, Vec3 min, Vec3 max, int rgba) {
        STRenderUtil.drawFilledBox(buffer, matrix,
                (float) min.x(), (float) min.y(), (float) min.z(),
                (float) max.x(), (float) max.y(), (float) max.z(),
                rgba
        );
    }
    
    public static void drawFilledBox(VertexConsumer buffer, Matrix4f matrix, Vec3i min, Vec3i max, int rgba) {
        STRenderUtil.drawFilledBox(buffer, matrix,
                min.getX(), min.getY(), min.getZ(),
                max.getX(), max.getY(), max.getZ(),
                rgba
        );
    }
    
    public static void drawFilledBox(VertexConsumer buffer, Matrix4f matrix,
                                     float minX, float minY, float minZ,
                                     float maxX, float maxY, float maxZ,
                                     int rgba
    ) {
        int r = (rgba >> 24) & 0xFF;
        int g = (rgba >> 16) & 0xFF;
        int b = (rgba >> 8) & 0xFF;
        int a = rgba & 0xFF;
        
        // Bottom (Y-)
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        
        // Top (Y+)
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        
        // North (Z-)
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        
        // South (Z+)
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        
        // West (X-)
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        
        // East (X+)
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
    }
    
    public static void drawQuad(VertexConsumer buffer, Matrix4f matrix, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, int rgba) {
        STRenderUtil.drawQuad(buffer, matrix,
                (float) v1.x(), (float) v1.y(), (float) v1.z(),
                (float) v2.x(), (float) v2.y(), (float) v2.z(),
                (float) v3.x(), (float) v3.y(), (float) v3.z(),
                (float) v4.x(), (float) v4.y(), (float) v4.z(),
                rgba);
    }
    
    public static void drawQuad(VertexConsumer buffer, Matrix4f matrix,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float x3, float y3, float z3,
                                float x4, float y4, float z4,
                                int rgba
    ) {
        int r = (rgba >> 24) & 0xFF;
        int g = (rgba >> 16) & 0xFF;
        int b = (rgba >> 8) & 0xFF;
        int a = rgba & 0xFF;
        
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
        buffer.addVertex(matrix, x4, y4, z4).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, x4, y4, z4).setColor(r, g, b, a);
        buffer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
    }
    
    public static void drawLine(VertexConsumer buffer, Matrix4f matrix, BlockPos start, BlockPos end, int rgba, float width) {
        STRenderUtil.drawLine(buffer, matrix, new Vec3(start), new Vec3(end), rgba, width);
    }
    
    public static void drawLine(VertexConsumer buffer, Matrix4f matrix, Vec3i start, Vec3i end, int rgba, float width) {
        STRenderUtil.drawLine(buffer, matrix, new Vec3(start), new Vec3(end), rgba, width);
    }
    
    public static void drawLine(VertexConsumer buffer, Matrix4f matrix, Vec3 start, Vec3 end, int rgba, float width) {
        STRenderUtil.drawLine(buffer, matrix, (float) start.x(), (float) start.y(), (float) start.z(), (float) end.x(), (float) end.y(), (float) end.z(), rgba, width);
    }
    
    public static void drawLine(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int rgba, float width) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        
        float length = Mth.lengthSquared(dx, dy, dz);
        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }
        
        int r = (rgba >> 24) & 0xFF;
        int g = (rgba >> 16) & 0xFF;
        int b = (rgba >> 8) & 0xFF;
        int a = rgba & 0xFF;
        
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(dx, dy, dz).setLineWidth(width);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(dx, dy, dz).setLineWidth(width);
    }
}
