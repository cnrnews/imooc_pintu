package pintu.imooc.com.imooc_pintu.utils;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import pintu.imooc.com.imooc_pintu.entity.ImagePiece;

/**
 * @author：lihl on 2017/11/12 18:11
 * @email：1601796593@qq.com
 */
public class ImageSplitterUtils {

    /***
     * 切图
     * @param bitmap 原图
     * @param piece 切片大小
     * @return
     */
    public static List<ImagePiece> spliteImage(
            Bitmap bitmap, int piece) {
        List<ImagePiece> pieceList = new ArrayList<>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pieceWidth = Math.min(width, height) / piece;
        for (int i = 0; i < piece; i++) {
            for (int j = 0; j < piece; j++) {
                ImagePiece imagePiece = new ImagePiece();
                imagePiece.setIndex(j + i * piece);
                int x = j * pieceWidth;
                int y = i * pieceWidth;
                imagePiece.setBitmap(Bitmap.createBitmap(
                        bitmap, x, y, pieceWidth, pieceWidth));
                pieceList.add(imagePiece);
            }
        }
        return pieceList;
    }
}
