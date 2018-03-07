package com.mx.hb.moon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mx.hb.moon.R;
import com.mx.hb.moon.base.BaseTools;
import com.mx.hb.moon.base.Constants;
import com.mx.hb.moon.base.Encoder;
import com.mx.hb.moon.base.PrefShared;
import com.mx.hb.moon.dialog.NormalDialog;
import com.mx.hb.moon.dialog.OnBtnClickL;
import com.mx.hb.moon.loading.SVProgressHUD;
import com.mx.hb.moon.view.StatusBarUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Moon on 2016/4/18.
 */
public class ShareActivity1 extends BaseActivity implements View.OnClickListener{
    private static SVProgressHUD mSVProgressHUD;
    private IWXAPI iwxapi;
    Bitmap bitmap;
    private Encoder mEncoder;
    private ImageView QRCode;
    private String createEncode;
    private Button shareBtn;
    private LinearLayout onBack;
    //自定义的弹出框类
    private PhonePopupWindow menuWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        mSVProgressHUD = new SVProgressHUD(context);
        initView();
        initWeixin();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.share),0);
    }

    private void initView() {
        QRCode = (ImageView) findViewById(R.id.QRCode);
        shareBtn = (Button) findViewById(R.id.shareBtn);
        onBack = (LinearLayout) findViewById(R.id.onBack);
        QRCode.setOnClickListener(this);
        onBack.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        createQRCode();
    }

    /**
     * 生成二维码
     */
    private void createQRCode(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEncoder = new Encoder.Builder()
                        .setBackgroundColor(0xffffffff) // 指定背景颜色，默认为白色
                        .setCodeColor(0xff000000) // 指定编码块颜色，默认为黑色
                        .setOutputBitmapWidth((int) BaseTools.dpChangePx(ShareActivity1.this,100)) // 生成图片宽度
                        .setOutputBitmapHeight((int) BaseTools.dpChangePx(ShareActivity1.this,100)) // 生成图片高度
                        .setOutputBitmapPadding(1) // 设置为没有白边
                        .build();
                createEncode = PrefShared.getString(context,"qrcode");
                bitmap = mEncoder.encode(createEncode);
                QRCode.setImageBitmap(bitmap);
            }
        });
    }

    /**
     * 初始化微信SDK
     */
    private void initWeixin() {
        iwxapi = WXAPIFactory.createWXAPI(this, Constants.WX_APPID, false);
        iwxapi.registerApp(Constants.WX_APPID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.shareBtn:
                showSharePopupWindow();
                break;
            case R.id.onBack:
                this.finish();
                break;
            default:
                break;
        }
    }

    public void saveImageToGallery(final Context context, final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 首先保存图片
                File appDir = new File(Environment.getExternalStorageDirectory(), Constants.CACHEDIRECTORY_PATH + "images");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                String fileName = PrefShared.getString(context, "UUID") + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 其次把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
                    // 最后通知图库更新
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,	Uri.fromFile(new File(file.getPath()))));
                    dialog("","保存到相册成功",Color.parseColor("#0876FE"),"可以在微信扫一扫，打开相册中的二维码或发送给微信好友",15,Gravity.CENTER,"","OK");
                } catch (FileNotFoundException e) {
                    Toast.makeText(ShareActivity1.this,"保存失败",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     *
     * @param type
     * @param title
     * @param titleColor
     * @param content
     * @param contentSize
     * @param contentGravity
     * @param leftBtn
     * @param rightBtn
     */
    private void dialog(final String type,String title,int titleColor,
                                  String content,int contentSize,int contentGravity,
                                  String leftBtn, String rightBtn) {
        final NormalDialog dialog = new NormalDialog(ShareActivity1.this);
        /*弹窗*/
        dialog.widthScale((float) 0.75);//设整个弹窗的宽度
        dialog.cornerRadius(8);//设弹窗的圆角
        dialog.style(NormalDialog.STYLE_TWO);//设为两个按钮
        dialog.isTitleShow(true);//显示标题
        dialog.bgColor(getResources().getColor(R.color.white));//设弹窗的背景颜色
        dialog.btnNum(1);//设置弹窗只有一个按钮
        /*标题*/
        dialog.title(title);//设标题
        dialog.titleTextSize(20);//设标题字体大小
        dialog.titleTextColor(titleColor);//设标题样色 Color.parseColor("#000000")
        /*内容*/
        dialog.content(content);//设内容
        dialog.contentTextSize(contentSize);//设内容的字体大小
        dialog.contentTextColor(getResources().getColor(R.color.black));//设内容的字体颜色 Color.parseColor("#000000")
        dialog.contentGravity(contentGravity);//设内容显示的位置

        dialog.dividerColor(Color.parseColor("#CCCCCC"));//

        /*按钮*/
        dialog.btnText(rightBtn);//设按钮的文字内容
        dialog.btnTextSize(15.5f, 15.5f);//设按钮的字体大小
        dialog.btnTextColor(getResources().getColor(R.color.dialog_blue));//设按钮的字体颜色
        dialog.btnPressColor(Color.parseColor("#E5E5E5"));//设按钮点击时的背景颜色
        dialog.show();

        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
            }
        });
    }

//    public static void dialog() {
//        final NormalDialog dialog = new NormalDialog(context);
//        dialog.widthScale((float) 0.75);
//        dialog.title("保存到相册成功");
//        dialog.titleTextSize(20);
//        dialog.titleLineColor(Color.parseColor("#FFFFFF"));
//        dialog.contentTextSize(15);
//        dialog.style(NormalDialog.STYLE_ONE);
//        dialog.titleTextColor(Color.parseColor("#0876FE"));
//        dialog.isTitleShow(true)
//                //
//                .bgColor(Color.parseColor("#FFFFFF"))
//                //
//                .cornerRadius(8)
//                //
//                .content("可以在微信扫一扫，打开相册中的二维码或发送给微信好友")
//                //
//                .contentGravity(Gravity.CENTER)
//                //
//                .contentTextColor(Color.parseColor("#000000"))
//                //
//                .dividerColor(Color.parseColor("#CCCCCC"))
//                //
//                .btnTextSize(15.5f)
//                //
//                .btnNum(1)
//                .btnText("OK")
//                .btnTextColor(Color.parseColor("#0876FE"))//
//                .btnPressColor(Color.parseColor("#E5E5E5"))//
//                .show();
//
//        dialog.setOnBtnClickL(new OnBtnClickL() {
//            @Override
//            public void onBtnClick() {
//                dialog.dismiss();
//            }
//        });
//    }

    /**
     * 显示底部弹框
     */
    private void showSharePopupWindow() {
        menuWindow = new PhonePopupWindow(ShareActivity1.this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //显示窗口
        menuWindow.showAtLocation(shareBtn, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    //从底部弹出的PhonePopupWindow继承PopupWindow
    public class PhonePopupWindow extends PopupWindow {

        private View viewmenu;
        private TextView friends,friend,link,code,cancel;

        public PhonePopupWindow(final Activity context, View.OnClickListener onClickListener) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewmenu = inflater.inflate(R.layout.window_weishar, null);
            friends = (TextView) viewmenu.findViewById(R.id.friends);
            friend = (TextView) viewmenu.findViewById(R.id.friend);
            link = (TextView) viewmenu.findViewById(R.id.link);
            code = (TextView) viewmenu.findViewById(R.id.code);
            cancel = (TextView) viewmenu.findViewById(R.id.cancel);
            this.setContentView(viewmenu);
            this.setWidth(BaseTools.getWindowsWidth(context));////设置窗体的高
            this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);//设置窗体的高
            // 设置窗体为透明效果
            ColorDrawable cd = new ColorDrawable(0x000000);
            this.setBackgroundDrawable(cd);
            this.setFocusable(true);//设置窗体可点击
            this.setAnimationStyle(R.style.AnimBottom);//设置窗体从底部进入的动画效果
            this.setOutsideTouchable(true);// 点击外部可关闭窗口
            this.update();
            //关闭窗体时
            this.setOnDismissListener(new OnDismissListener() {
                // 在dismiss中恢复透明度
                public void onDismiss() {
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.alpha = 1f;
                    getWindow().setAttributes(lp);
                }
            });
            // 打开窗口时设置窗体背景透明度
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 0.8f;
            getWindow().setAttributes(lp);

            friends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    weChatShare(1);
                }
            });

            friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    weChatShare(0);
                }
            });
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(createEncode);
                    dialog("","已复制邀请链接",Color.parseColor("#0876FE"),createEncode+"\n可发送该链接给[微信好友]\n(该链接仅微信中使用)",15,Gravity.CENTER,"","OK");
                }
            });
            code.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
//                    BaseTools.base64ToBitmap(BaseTools.bitmapToBase64(bitmap))
                    saveImageToGallery(context, bitmap);
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            viewmenu.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        dismiss();
                    }
                    return true;
                }
            });
        }
    }

    /**
     *
     * @param type 1为朋友圈 0为好友
     */
    private void weChatShare(final int type) {
        if (iwxapi.isWXAppInstalled() == false) {
            mSVProgressHUD.showInfoWithStatus("请先安装微信", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
        } else {
            if (!iwxapi.isWXAppSupportAPI()) {
                mSVProgressHUD.showInfoWithStatus("微信版本不支持", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else {
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = createEncode;
                WXMediaMessage msg = new WXMediaMessage(webpage);
                //这里替换一张自己工程里的图片资源
                Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                msg.setThumbImage(thumb);
                if (type == 1) {
                    msg.title = "立即领取现金2元，已累计发放红包3800万，加入来赚每月轻松收入上千！";
                } else {
                    msg.title = "现金2元，限时领取";
                    msg.description = "1分钟赚2元，还有更快的吗？已累计发放红包3800万，快来领取！";
                }
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = String.valueOf(System.currentTimeMillis());
                req.message = msg;
                req.scene = type == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
                iwxapi.sendReq(req);
            }
        }
    }

    /**
     * 微信分享的回调
     * @param errCode shareType
     */
    public static void getShareState(int errCode,int shareType) {
        if (shareType == 1) {
            if(errCode == 0) {
                mSVProgressHUD.showSuccessWithStatus("登录成功", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if (errCode == -2) {
                mSVProgressHUD.showInfoWithStatus("用户取消操作", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if (errCode == -3) {
                mSVProgressHUD.showErrorWithStatus("操作失败", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if (errCode == -4) {
                mSVProgressHUD.showInfoWithStatus("用户拒绝授权", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else {
                mSVProgressHUD.showErrorWithStatus("操作失败", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            }
        } else {
            if(errCode == 0) {
                mSVProgressHUD.showSuccessWithStatus("分享成功", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if (errCode == -2) {
                mSVProgressHUD.showInfoWithStatus("用户取消操作", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if (errCode == -3) {
                mSVProgressHUD.showErrorWithStatus("操作失败", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else if (errCode == -4) {
                mSVProgressHUD.showInfoWithStatus("用户拒绝授权", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } else {
                mSVProgressHUD.showErrorWithStatus("操作失败", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
