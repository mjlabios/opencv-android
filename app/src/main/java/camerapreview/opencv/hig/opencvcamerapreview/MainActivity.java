package camerapreview.opencv.hig.opencvcamerapreview;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileDescriptor;
import java.io.IOException;


public class MainActivity extends Activity {

    private static final int RESULT_LOAD_IMAGE1 = 1;
    private static final int RESULT_LOAD_IMAGE2 = 2;
    private static final int ONMANAGER_INIT = 696969;

    private Mat ImageMat,ImageMat2,ImageMat3,ImageMat4;
    private Bitmap img1,img2;
    private Button button1;
    private  TextView log;
    private int IMAGE_1 = R.drawable.iphone1;
    private int IMAGE_2 = R.drawable.iphone1b;
    private ImageView imageView,imageView2;
    private Mini man;
    private BitmapFactory.Options options;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    options = new BitmapFactory.Options();
                    options.inSampleSize = 6;

                    Log.i("OpenCV", "OpenCV loaded successfully");
                    img1 = BitmapFactory.decodeResource(getBaseContext().getResources(),
                            IMAGE_1, options);
                   // img1 = toGrayscale(img1);

                    img2  = BitmapFactory.decodeResource(getBaseContext().getResources(),
                            IMAGE_2, options);
                   // img2 = toGrayscale(img2);


                case ONMANAGER_INIT:
                    Log.i("MOSBY","initialize image bitmap");

                   initializeMat();

                 break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }

        public void initializeMat(){
            ImageMat = new Mat(img1.getWidth(),img1.getHeight(), CvType.CV_8UC1);
            ImageMat2 = new Mat(img2.getWidth(),img2.getHeight(), CvType.CV_8UC1);
        }
    };

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        button1 = (Button) findViewById(R.id.button1);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initProcess();
                initFinal();
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE1);
            }
        });

        imageView2 = (ImageView) findViewById(R.id.imageView2);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE2);
            }
        });

        log = (TextView)findViewById(R.id.log);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bmp = null;
            switch(requestCode){
                case RESULT_LOAD_IMAGE1:  try {
                                            img1 = getBitmapFromUri(selectedImage);

                                        } catch (IOException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                    imageView.setImageBitmap(img1);
                    mLoaderCallback.onManagerConnected(ONMANAGER_INIT);
                    break;
                case RESULT_LOAD_IMAGE2:  try {
                                            img2 = getBitmapFromUri(selectedImage);

                                        } catch (IOException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                    imageView2.setImageBitmap(img2);
                    mLoaderCallback.onManagerConnected(ONMANAGER_INIT);
                                            break;
            }


        }


    }



    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void initProcess(){
        Utils.bitmapToMat(img1, ImageMat);
        Utils.bitmapToMat(img2, ImageMat2);
        img1 = toGrayscale(img1);
        img2 = toGrayscale(img2);
    }

    private void initFinal(){
        man = new Mini();
        if(man.compare(ImageMat,ImageMat2)) {
            log.setText("similarity detected");
        }
        else{
            Utils.bitmapToMat(img1, ImageMat);
            Utils.bitmapToMat(img2, ImageMat2);
            if(man.compare(ImageMat,ImageMat2)){
                log.setText("similarity detected");
            }else {
                log.setText("no similarity detected");
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        img1.recycle();
        img1 = null;

        img2.recycle();
        img2 = null;
    }
}
