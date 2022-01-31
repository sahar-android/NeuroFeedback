package ir.simyapp.neurogame.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ir.simyapp.neurogame.MainActivity;
import ir.simyapp.neurogame.R;

public class GameControllerService extends Service {
    public static final int MESSAGE = 1;
    Messenger replyMessanger;
    float rand;
    public static final int notify = 40;
    private Handler mHandler = new Handler();
    private Timer myTimer=null;
    public static String BUNDLE_KEY="controller_value";
    /*static Service mService;
    private View gameView;
    private WindowManager mWindowManager;
    private  WindowManager.LayoutParams windowParams;
    private FrameLayout main_fl;*/


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.i("rtrtrtrt", "handleMessage1: " + msg.toString());
            switch (msg.what) {
                case 1:
                    replyMessanger = msg.replyTo;
                    startGame();
                    stopGame();
                    break;
                case 2:
                    Log.i("rtrtrtrt", "handleMessage: "+msg.what);
                    myTimer.cancel();

                    int finalScore=msg.arg1;
                    resetGame();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }




    final Messenger messenger = new Messenger(new IncomingHandler());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private void startGame() {
        if (replyMessanger != null){
            Message message = new Message();
            message.what = 0x11;
            try {
                replyMessanger.send(message);
                sendData();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }


    }

    private void sendData() {
        if (replyMessanger != null) {
            if (myTimer != null){
                myTimer.cancel();
            } else{
                myTimer=new Timer();
                myTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);
            }

        }
    }


    private float dataProduct(float max, float min) {
        Random random = new Random();
        rand = random.nextFloat() * (max - min) + min;
        return rand;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        myTimer.cancel();
    }

    private void resetGame() {
        if (replyMessanger != null){
            Message message=new Message();
            message.what=0x13;
            try {
                replyMessanger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopGame() {
        if (replyMessanger != null)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 0x14;
                    try {
                        replyMessanger.send(message);
                        sendData();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 20000);
    }

    class TimeDisplay extends TimerTask{

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putFloat(BUNDLE_KEY, dataProduct(2, 0));
                    msg.setData(bundle);
                    msg.what = 0x12;
                    try {
                        replyMessanger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
/*        Context context=getApplicationContext();
        mService=this;
        gameView = LayoutInflater.from(mService).inflate(R.layout.game_layout, null);
        main_fl=gameView.findViewById(R.id.main_fl);
        //customView=new MyCustomView(getApplicationContext());
        //main_fl.addView(customView);
        //Add the view to the window.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            windowParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }else {
            windowParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        windowParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL ;
        windowParams.x = 200;
        windowParams.y = 200;
        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if(mWindowManager!=null)
            mWindowManager.addView(gameView, windowParams);*/


    }

}
