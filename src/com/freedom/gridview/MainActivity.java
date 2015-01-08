package com.freedom.gridview;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;

import com.freedom.gridview.adapter.GridViewAdapter;
import com.freedom.gridview.bean.Data;

public class MainActivity extends Activity {
	/**
	 * 桌子布局
	 */
	private FreedomGridView mGridView;
	private GridViewAdapter adapter;
	private List<Data> dataResults;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mGridView = (FreedomGridView) findViewById(R.id.gridview);
		Random number = new Random();
		dataResults = new ArrayList<Data>();
		for (int i = 0; i < 10; i++) {
			Data data = new Data(i, number.nextInt(3));
			dataResults.add(data);
		}
		adapter = new GridViewAdapter(getBaseContext(), dataResults);
		mGridView.setAdapter(adapter);
	}

}
