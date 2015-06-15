package com.antilost.app.loginfisrt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.antilost.app.R;
import com.antilost.app.activity.LoginActivity;

import java.util.Locale;

public class ScrollLayoutActivity extends Activity implements OnViewChangeListener{
   
	private ScrollLayout mScrollLayout;
	private ImageView[] imgs;
	private int count;
	private int currentItem;
	private Button startBtn;
	private RelativeLayout mainRLayout;
	private LinearLayout pointLLayout;
	private LinearLayout leftLayout;
	private LinearLayout rightLayout;
	private LinearLayout animLayout;
	private RelativeLayout firistRLayout;
	private RelativeLayout secondRLayout;
	private RelativeLayout threeRLayout;
	private String TAG = "ScrollLayoutActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginfirst);
		initView();
	}

	private void initView() {
		mScrollLayout = (ScrollLayout) findViewById(R.id.ScrollLayout);
		pointLLayout = (LinearLayout) findViewById(R.id.llayout);
		mainRLayout = (RelativeLayout) findViewById(R.id.mainRLayout);
		firistRLayout = (RelativeLayout) findViewById(R.id.rlfirstPage);
		secondRLayout = (RelativeLayout) findViewById(R.id.rlsecondPage);
		threeRLayout = (RelativeLayout) findViewById(R.id.rlthreePage);
		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(onClick);
//		animLayout = (LinearLayout) findViewById(R.id.animLayout);
//		leftLayout = (LinearLayout) findViewById(R.id.leftLayout);
//		rightLayout = (LinearLayout) findViewById(R.id.rightLayout);
		//语言包
		Locale l = Locale.getDefault();
		String language = l.getLanguage();
		Log.d(TAG,"The Language is "+language);
		if(language.equals("zh")){
			firistRLayout.setBackground(getResources().getDrawable(R.drawable.zh_w001));
			secondRLayout.setBackground(getResources().getDrawable(R.drawable.zh_w002));
			threeRLayout.setBackground(getResources().getDrawable(R.drawable.zh_w003));
		}else if(language.equals("en")){
			firistRLayout.setBackground(getResources().getDrawable(R.drawable.w001));
			secondRLayout.setBackground(getResources().getDrawable(R.drawable.w002));
			threeRLayout.setBackground(getResources().getDrawable(R.drawable.w003));
		}else if(language.equals("fr")){
			firistRLayout.setBackground(getResources().getDrawable(R.drawable.fr_w001));
			secondRLayout.setBackground(getResources().getDrawable(R.drawable.fr_w002));
			threeRLayout.setBackground(getResources().getDrawable(R.drawable.fr_w003));
		}
		count = mScrollLayout.getChildCount();
		imgs = new ImageView[count];
		for (int i = 0; i < count; i++) {
			imgs[i] = (ImageView) pointLLayout.getChildAt(i);
			imgs[i].setEnabled(true);
			imgs[i].setTag(i);
		}
		currentItem = 0;
		imgs[currentItem].setEnabled(false);
		mScrollLayout.SetOnViewChangeListener(this);
	}

	private View.OnClickListener onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.startBtn:
				Intent intent = new Intent(
						ScrollLayoutActivity.this,
						LoginActivity.class);
				ScrollLayoutActivity.this.startActivity(intent);
				ScrollLayoutActivity.this.finish();
//				mScrollLayout.setVisibility(View.GONE);
//				pointLLayout.setVisibility(View.GONE);
//				animLayout.setVisibility(View.VISIBLE);
//				mainRLayout.setBackgroundResource(R.drawable.whatsnew_bg);
//				Animation leftOutAnimation = AnimationUtils.loadAnimation(
//						getApplicationContext(), R.anim.translate_left);
//				Animation rightOutAnimation = AnimationUtils.loadAnimation(
//						getApplicationContext(), R.anim.translate_right);
//				leftLayout.setAnimation(leftOutAnimation);
//				rightLayout.setAnimation(rightOutAnimation);
//				leftOutAnimation.setAnimationListener(new AnimationListener() {
//					@Override
//					public void onAnimationStart(Animation animation) {
//						mainRLayout.setBackgroundColor(Color.BLACK);
//					}
//
//					@Override
//					public void onAnimationRepeat(Animation animation) {
//					}
//
//					@Override
//					public void onAnimationEnd(Animation animation) {
//						leftLayout.setVisibility(View.GONE);
//						rightLayout.setVisibility(View.GONE);
//						Intent intent = new Intent(
//								ScrollLayoutActivity.this,
//								OtherActivity.class);
//						ScrollLayoutActivity.this.startActivity(intent);
//						ScrollLayoutActivity.this.finish();
//						//缁撴潫鑰丄ctivity鍚姩鏂癆ctivity涔嬪墠鐨勪竴涓繃搴﹀姩鐢�						overridePendingTransition(R.anim.zoom_out_enter,R.anim.zoom_out_exit);
//					}
//				});
				break;
			}
		}
	};

	@Override
	public void OnViewChange(int position) {
		setcurrentPoint(position);
	}

	private void setcurrentPoint(int position) {
		if (position < 0 || position > count - 1 || currentItem == position) {
			return;
		}
		imgs[currentItem].setEnabled(true);
		imgs[position].setEnabled(false);
		currentItem = position;
	}
}