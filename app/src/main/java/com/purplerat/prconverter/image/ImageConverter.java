package com.purplerat.prconverter.image;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageConverter implements Runnable{
    private final Uri importFile;
    private final int x,y;
    private final File exportFile;
    private final ImageConverterCallback imageConverterCallback;
    private final Context context;
    public ImageConverter(final Context context,final Uri importFile, final int x, final int y, final File exportFile, ImageConverterCallback imageConverterCallback){
        this.context = context;
        this.importFile = importFile;
        this.x = x;
        this.y = y;
        this.exportFile = exportFile;
        this.imageConverterCallback = imageConverterCallback;
        //System.out.println(importFile.getPath() + '\t' + x + '\t' + y + '\t' + exportFile.getAbsolutePath());
    }
    @Override
    public void run() {
        imageConverterCallback.onComplete(convert());
    }
    private File convert(){
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),importFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Bitmap exportBitmap = Bitmap.createScaledBitmap(bitmap, x, y,true);
        Bitmap.CompressFormat imageFormat;
        switch (exportFile.getName().substring(exportFile.getName().lastIndexOf(".")+1)){
            case "jpeg":
                imageFormat = Bitmap.CompressFormat.JPEG;
                break;
            case"png":
                imageFormat = Bitmap.CompressFormat.PNG;
                break;
            case"webp":
                imageFormat = Bitmap.CompressFormat.WEBP;
                break;
            default:
                //System.out.println(exportFile.getName().substring(exportFile.getName().lastIndexOf(".")));
                return null;
        }
        try {
            exportBitmap.compress(imageFormat,100,new FileOutputStream(exportFile));
            MediaScannerConnection.scanFile(context,new String[]{exportFile.getPath()},new String[] {getMimeType(Uri.fromFile(exportFile))},null);
            return exportFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    private String getMimeType(Uri uri){
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }
    public interface ImageConverterCallback{
        void onComplete(File file);
    }
}
