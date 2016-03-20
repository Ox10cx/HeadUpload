package gx.com.headupload;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;



public class MainActivity extends Activity {
    //服务器的IP地址
    String url = "http://192.168.1.3:8080/Sanzhizhaimao/headUploadServlet";

    //上传图片对应的 姓名
    String userName = "四代火影";
    private Context context;

    private RelativeLayout re_avatar;


    private ImageView iv_avatar;
    private String urlpath;


    private String imageName;
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);
        context=MainActivity.this;
        initView();

    }

    private void initView() {


        re_avatar = (RelativeLayout) this.findViewById(R.id.re_avatar);

        re_avatar.setOnClickListener(new MyListener());

        iv_avatar = (ImageView) this.findViewById(R.id.iv_avatar);


    }

    class MyListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.re_avatar:
                    showPhotoDialog();
                    break;
            }
        }


    }

    private void showPhotoDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(R.layout.alertdialog);
        // 为确认按钮添加事件,执行退出应用操作
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("拍照");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {


                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                FileUtils.savePath(String.valueOf(System.currentTimeMillis()));
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(FileUtils.picPath)));
                Log.d("ss", "拍照前路径" + FileUtils.picPath);
                startActivityForResult(openCameraIntent, PHOTO_REQUEST_TAKEPHOTO);
                dlg.cancel();

            }
        });
        TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
        tv_xiangce.setText("相册");
        tv_xiangce.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                Intent intent = new Intent(Intent.ACTION_PICK, null);

                       intent.setDataAndType(
                      MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");


                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);

                dlg.cancel();
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST_TAKEPHOTO:

                    Log.d("ss", "调用了相机拍摄" + FileUtils.picPath);


                    startPhotoZoom(
                            Uri.fromFile(new File(FileUtils.picPath))
                         );
                    break;

                case PHOTO_REQUEST_GALLERY:
                    if (data != null) {
                        startPhotoZoom(data.getData());
                    }
//                    FileUtils.picPath = getPath(data.getData());
//                    Log.d("ss", "选中了" + FileUtils.picPath);

                    break;

                case PHOTO_REQUEST_CUT:
                    // BitmapFactory.Options options = new BitmapFactory.Options();
                    //
                    // /**
                    // * 最关键在此，把options.inJustDecodeBounds = true;
                    // * 这里再decodeFile()，返回的bitmap为空
                    // * ，但此时调用options.outHeight时，已经包含了图片的高了
                    // */
                    // options.inJustDecodeBounds = true;
//                    Log.d("ss", "执行了照片裁剪");
//                    Bitmap bitmap =data.getParcelableExtra("data");
//
//                    iv_avatar.setImageBitmap(bitmap);
//                    //updateAvatarInServer(imageName);
//                    new UploadTask().execute(userName);
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        iv_avatar.setImageBitmap(photo);
//                        Log.d("ss",photo.toString());
                        urlpath = FileUtils.saveBitmap(photo, "szzm");
//                        Drawable drawable = new BitmapDrawable(null, photo);
//                        iv_avatar.setImageDrawable(drawable);

                    }



//                        //头像上传
//                        try {
//                            //将用户名进行转码
//                            new UploadTask().execute(URLEncoder.encode(userName,"utf-8"));
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }

                    break;

            }
            super.onActivityResult(requestCode, resultCode, data);

        }
    }
    /**
     * 保存裁剪之后的图片数据
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            urlpath = FileUtils.saveBitmap(photo,"szzm");
            Drawable drawable = new BitmapDrawable(null, photo);
            iv_avatar.setImageDrawable(drawable);

        }
    }

    class UploadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,10000);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,10000);
            HttpPost post = new HttpPost(url);
            MultipartEntity entity = new MultipartEntity();
            try {
                entity.addPart("userName", new StringBody(params[0]));
                File file = new File(urlpath);
                Log.d("ss",urlpath);
                Log.d("ss", "上传的图片路径" + urlpath);
                entity.addPart("file", new FileBody(file));


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(entity);

            //从客户端发送信息

          try {
				HttpResponse response=client.execute(post);
				Log.d("ss","编号"+response.getStatusLine().getStatusCode());

				if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode())
				{
					String result= EntityUtils.toString(response.getEntity());
					try {
						JSONObject jsstr=new JSONObject(result);
						String str=jsstr.getString("type");
						if(str.equals("head_ok"))
						{
							return "更新成功";
						}
						if(str.equals("head_error"))
						{
							return "更新失败";
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
            return "网络异常";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("SdCardPath")
    private void startPhotoZoom(Uri uri1) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri1, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 200);
        intent.putExtra("aspectY", 150);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 150);
       // intent.putExtra("return-data", false);

//        intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                Uri.fromFile(new File(FileUtils.picPath)));

        intent.putExtra("return-data", true);// true:不返回uri，false：返回uri
        //intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
       // intent.putExtra("noFaceDetection", true); // no face detection
        Log.d("ss", "照片执行方法" + FileUtils.picPath);

        startActivityForResult(intent, PHOTO_REQUEST_CUT);

    }





}








