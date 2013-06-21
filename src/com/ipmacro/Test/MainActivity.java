package com.ipmacro.Test;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {
	Context mContext;

	private List<String> getData() {
		List<String> data = new ArrayList<String>();
		data.add("LoginActivity");
		data.add("RlpDownloadActivity");
		data.add("AndPlayerActivity");
		data.add("LiveActivity");
		data.add("FlvAppleActivity");
		data.add("TsAppleActivity");
		data.add("MutiplayActivity");
		return data;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, getData()));

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String className = (String) getListAdapter().getItem(position);
		String packName = getPackageName();

		Intent intent = new Intent();
		intent.setClassName(mContext, packName + "." + className);
		mContext.startActivity(intent);
	}
}
