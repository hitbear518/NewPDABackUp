package com.zsxj.pda.ui.common;

import com.zsxj.pda.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BaseActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	protected void go(Class<?> cls, Object json, Object ctx)
	{
		Intent it = new Intent();
		it.setClass(this, cls);
		it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		if(json != null)
		{
			it.putExtra("json", json.toString());
		}
		
		if(ctx != null)
		{
			it.putExtra("ctx", ctx.toString());
		}
		
		this.startActivity(it);
	}
	
	protected void setRightButton(View.OnClickListener click)
	{
		View right_button = this.findViewById(R.id.right_btn);
		right_button.setOnClickListener(click);
	}
	
	protected void setLeftButton(View.OnClickListener click)
	{
		View left_button = this.findViewById(R.id.left_btn);
		left_button.setOnClickListener(click);
	}
	
	protected void setBackButton()
	{
		setLeftButton(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
			}
		});
	}
	
	protected void setTitle(String title)
	{
		((TextView)this.findViewById(R.id.title_text)).setText(title);
	}
	
}