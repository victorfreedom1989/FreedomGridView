package com.freedom.gridview.adapter;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.freedom.gridview.DptoPxUtil;
import com.freedom.gridview.R;
import com.freedom.gridview.bean.Data;

/**
 * 主界面GridView适配器
 */
public class GridViewAdapter extends BaseAdapter {
	private Context context;
	private String TAG = "DeskGridViewAdapter";
	private List<Data> deskResults;

	public GridViewAdapter(Context context, List<Data> deskResults) {
		this.deskResults = deskResults;
		this.context = context;
	}

	@Override
	public int getCount() {
		return deskResults.size();
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.desk_item, null);
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					DptoPxUtil.dip2px(context, 150), DptoPxUtil.dip2px(context,
							150));
			convertView.setLayoutParams(params);
			holder.num = (TextView) convertView
					.findViewById(R.id.desk_detail_name);
			holder.status = (ImageView) convertView
					.findViewById(R.id.desk_back);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Data data = deskResults.get(position);
		int num = data.getNum();
		switch (num) {
		case 0:
			holder.num.setText("0");
			holder.status.setImageResource(R.drawable.desk_gray);
			break;
		case 1:
			holder.num.setText("1");
			holder.status.setImageResource(R.drawable.desk_orange);
			break;
		default:
			holder.num.setText("2");
			holder.status.setImageResource(R.drawable.desk_honer);
			break;
		}
		return convertView;
	}

	@Override
	public Object getItem(int position) {
		return deskResults.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class ViewHolder {
		TextView num;
		ImageView status;
	}

}