package com.sismics.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImagingOpException;

/**
 * Extends Scalr.
 *
 * @author bgamard
 */
public class Scalr extends org.imgscalr.Scalr {
    /**
     * Rotate an image by a specific amount.
     *
     * @param src Source image
     * @param rotation Rotation angle
     * @param ops Options
     * @return Rotated image
     * @throws IllegalArgumentException
     * @throws ImagingOpException
     */
    public static BufferedImage rotate(BufferedImage src, double rotation, BufferedImageOp... ops) throws IllegalArgumentException, ImagingOpException {
        long t = System.currentTimeMillis();
        if (src == null) {
            throw new IllegalArgumentException("src cannot be null");
        } else {
            if (DEBUG) {
                log(0, "Rotating Image [%s]...", rotation);
            }

            // 计算旋转后图像的尺寸
            double rads = Math.toRadians(rotation);
            double sin = Math.abs(Math.sin(rads));
            double cos = Math.abs(Math.cos(rads));
            int srcWidth = src.getWidth();
            int srcHeight = src.getHeight();
            int newWidth = (int) Math.floor(srcWidth * cos + srcHeight * sin);
            int newHeight = (int) Math.floor(srcHeight * cos + srcWidth * sin);

            // 创建足够大的新图像
            BufferedImage result = createOptimalImage(src, newWidth, newHeight);
            Graphics2D g2d = result.createGraphics();

            // 确保高质量渲染
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 设置旋转中心点到图像中心
            AffineTransform tx = new AffineTransform();
            tx.translate((newWidth - srcWidth) / 2.0, (newHeight - srcHeight) / 2.0);
            tx.rotate(rads, srcWidth / 2.0, srcHeight / 2.0);

            // 绘制旋转后的图像
            g2d.drawImage(src, tx, null);
            g2d.dispose();

            if (DEBUG) {
                log(0, "Rotation Applied in %d ms, result [width=%d, height=%d]", System.currentTimeMillis() - t, result.getWidth(), result.getHeight());
            }

            if (ops != null && ops.length > 0) {
                result = apply(result, ops);
            }

            return result;
        }
    }
}
