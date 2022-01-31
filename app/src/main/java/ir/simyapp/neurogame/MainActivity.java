package ir.simyapp.neurogame;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import ir.simyapp.neurogame.Services.GameControllerService;


public class MainActivity extends AppCompatActivity {
    FrameLayout main_fl;
    public static int x, y;
    Button start_btn;
    Paint mpaint = new Paint();
    private Canvas mCanvas;
    private MyCustomView customView;
    private Bundle bundle = new Bundle();
    private TextView tv_score,tv_start;
    private int scoreSum;
    private boolean finalFlg = false;


    /*private ActivityResultLauncher<Intent> activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==Activity.RESULT_OK){
                        Log.i("rtrtrtrttr", "onActivityResult: ");

                    }
                }
            });*/


    private ArrayList<Float> data = new ArrayList<>();
    private float min, max, rand;
    private GameControllerService mBoundService;
    Messenger mService = null;
    boolean mBound;
    ComponentName componentName;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
            sendData();
            componentName = className;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;

            Toast.makeText(mBoundService, "connection failed", Toast.LENGTH_SHORT).show();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_fl = findViewById(R.id.main_fl);
        start_btn = findViewById(R.id.start_btn);
        tv_score = findViewById(R.id.tv_score);
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        x = metrics.widthPixels;
        y = metrics.heightPixels;
        mpaint.setColor(getResources().getColor(R.color.lighred));
        customView = new MyCustomView(getApplicationContext());
        main_fl.addView(customView);
        tv_start=new TextView(getApplicationContext());
        tv_start.setGravity(Gravity.CENTER);
        tv_start.setTextSize(30);
        tv_start.setTextColor(getResources().getColor(R.color.black));
        main_fl.addView(tv_start);



        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CountDownTimer countDownTimer=new CountDownTimer(3000,1000) {
                    @Override
                    public void onTick(long l) {
                       tv_start.setText(String.valueOf(l/1000));
                    }

                    @Override
                    public void onFinish() {
                      tv_start.setText("");
                    }
                };
                countDownTimer.start();

                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            bindService(new Intent(MainActivity.this, GameControllerService.class), mConnection, Context.BIND_AUTO_CREATE);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                };
                handler.postDelayed(runnable, 3000);


            }
        });

    }


    public void sendData() {
        if (!mBound) return;
        try {
            Message message = Message.obtain(null, GameControllerService.MESSAGE, 1, 1);
            message.replyTo = replyMessenger;

            Bundle bundle = new Bundle();
            message.setData(bundle);
            mService.send(message);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //checkDrawOverlayPermission();
        // Bind to the service
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    Messenger replyMessenger = new Messenger(new HandlerReplyMsg());

    class HandlerReplyMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            float result;
            switch (msg.what) {
                case 17:
                    Toast.makeText(getApplicationContext(), "Your game has started", Toast.LENGTH_SHORT).show();
                    break;
                case 18:
                    bundle = msg.getData();
                    result = bundle.getFloat(GameControllerService.BUNDLE_KEY);
                    int score = Math.round(result);
                    scoreSum += score;
                    if (scoreSum <= 300) {
                        customView.setAlpha(result);
                        tv_score.setText(String.valueOf(scoreSum));
                    } else if (scoreSum > 300) {
                        sendScore(scoreSum);
                    }
                    break;

                case 19:
                    start_btn.setText("restet game");
                    break;
                case 20:
                    Log.i("rtrtrtrt", "msg: " + msg.toString());
                    Toast.makeText(getApplicationContext(), "game finished!", Toast.LENGTH_SHORT).show();
                    tv_start.setText("finished!");
                    //tv_start.setVisibility(View.VISIBLE);
                    break;

            }

        }

    }


    private void sendScore(int scoreSum) {
        Message message = new Message();
        message.arg1 = scoreSum;
        message.what = 0x02;
        message.replyTo = replyMessenger;
        if (replyMessenger != null) {
            try {
                Log.i("rtrtrtrt", "sendScore: " + message.toString());
                mService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }


    private void changeColor() {
        mpaint.setColor(getResources().getColor(R.color.lightbrown));
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim1);
        animation.setDuration(2000);
        customView.startAnimation(animation);
        customView.invalidate();
    }

    public class MyCustomView extends View {

        public MyCustomView(Context context) {
            super(context);
        }

        public MyCustomView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = canvas.getWidth(), h = canvas.getHeight(), x = MainActivity.x;
            super.onDraw(canvas);
            canvas.drawColor(0);
            mpaint.setStyle(Paint.Style.FILL);
            //canvas.drawCircle(w/2 , h/2 , x/3,paint1);

            Point a = new Point((3 * w - 2 * x) / 6, (h / 2) - 300);
            Point b = new Point((3 * w + 2 * x) / 6, (h / 2) - 300);

            float radius = x / 4;
            int startAngle = (int) (180 / Math.PI * Math.atan2(b.y - a.y, b.x - a.x));
            final RectF oval = new RectF();
            oval.set(a.x - 50, a.y - 2 * radius, b.x + 50, b.y + 2 * radius);
            canvas.drawOval(oval, mpaint);
            //canvas.drawArc(oval, 270F, 180F, true, paint1);

            Point c = new Point((w / 2), (h / 2));
            Point d = new Point((w / 2) + 120, (h / 2) + 600);
            Point f = new Point((w / 2) - 120, (h / 2) + 600);
            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(c.x, c.y);
            path.lineTo(d.x, d.y);
            path.lineTo(f.x, f.y);
            path.lineTo(c.x, c.y);
            path.close();
            canvas.drawPath(path, mpaint);

            Path path2 = new Path();
            path2 = new Path();
            Paint paint3 = new Paint();
            paint3.setColor(getResources().getColor(android.R.color.black));
            path2.moveTo(w / 2, h / 2 + 600);
            path2.cubicTo(w / 2 + 100, h / 2 + 500, w / 2, h / 2 + 800, w / 2 + 100, h / 2 + 1000);
            canvas.drawPath(path2, paint3);
        }

    }

    /* public void checkDrawOverlayPermission() {
     *//** check if we already  have permission to draw over other apps *//*
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            *//** if not construct intent to request permission *//*
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            *//** request permission via start activity for result *//*
            activityResultLauncher.launch(intent);
        }
    }*/

}