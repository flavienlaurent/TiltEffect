package com.fourmob.tilteffect;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TiltEffectAttacher.attach(findViewById(R.id.button));
		TiltEffectAttacher.attach(findViewById(R.id.imageView));
		WebView webView = (WebView) findViewById(R.id.webView);
		TiltEffectAttacher.attach(webView);
		webView.loadUrl("http://www.google.fr");
	}

}
