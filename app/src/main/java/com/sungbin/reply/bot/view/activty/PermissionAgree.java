package com.sungbin.reply.bot.view.activty;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Button;
import android.widget.RelativeLayout;

import com.fsn.cauly.CaulyAdInfo;
import com.fsn.cauly.CaulyAdInfoBuilder;
import com.fsn.cauly.CaulyAdView;
import com.fsn.cauly.CaulyAdViewListener;

import com.mommoo.permission.MommooPermission;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.utils.Utils;

public class PermissionAgree extends AppCompatActivity implements CaulyAdViewListener {

    private CaulyAdView javaAdView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_agree);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Utils.toast(this, getString(R.string.need_permission), FancyToast.LENGTH_LONG, FancyToast.WARNING);

        FancyToast.makeText(this, getString(R.string.need_notification_permission), FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);


        Button agree = (Button) findViewById(R.id.agree);
        agree.setOnClickListener(view -> {
            new MommooPermission.Builder(getApplicationContext())
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE)
                    .setPreNoticeDialogData(getString(R.string.plz_permission),getString(R.string.why_need_permission))
                    .setOnPermissionGranted(permissionList -> {
                        Utils.saveData(getApplicationContext(), "permission", "true");
                        Utils.toast(this, getString(R.string.agree_permission), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                        finish();
                        Intent i = new Intent(PermissionAgree.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    })
                    .setOfferGrantPermissionData(getString(R.string.permission_hand_give),getString(R.string.how_to_give_permission))
                    .build()
                    .checkPermissions();
        });

        CaulyAdInfo adInfo = new CaulyAdInfoBuilder(getString(R.string.ad_id)).
                effect("TopSlide").
                bannerHeight("Fixed_50").
                build();

        javaAdView = new CaulyAdView(this);
        javaAdView.setAdInfo(adInfo);
        javaAdView.setAdViewListener(this);

        RelativeLayout rootView = findViewById(R.id.adView);
        rootView.addView(javaAdView);

    }

    /*----- 배너 광고 -----*/
    @Override
    public void onReceiveAd(CaulyAdView adView, boolean isChargeableAd) {
        // 광고 수신 성공 & 노출된 경우 호출됨.
        // 수신된 광고가 무료 광고인 경우 isChargeableAd 값이 false 임.
    }
    @Override
    public void onFailedToReceiveAd(CaulyAdView adView, int errorCode, String errorMsg) {
        // 배너 광고 수신 실패할 경우 호출됨.
    }
    @Override
    public void onShowLandingScreen(CaulyAdView adView){
        // 광고 배너를 클릭하여 랜딩 페이지가 열린 경우 호출됨.
    }
    @Override
    public void onCloseLandingScreen(CaulyAdView adView) {
        // 광고 배너를 클릭하여 랜딩 페이지가 닫힌 경우 호출됨.
    }
    /*----- 배너 광고 끝 -----*/

}
