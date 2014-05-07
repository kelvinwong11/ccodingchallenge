package com.kwong.ccodingchallenge;

import java.util.ArrayList;

import com.kwong.ccodingchallenge.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends ArrayAdapter<ImageDetails>{

	Context context;
	int layoutResourceId;
	ArrayList<ImageDetails> imageData;
	
	public ImageAdapter(Context context, int layoutResourceId, ArrayList<ImageDetails> images) {
		super(context, layoutResourceId, images);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		imageData = images;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			v = inflater.inflate(layoutResourceId, parent, false);
		}
		
		ImageDetails img = imageData.get(position); 
		if (img != null) {
			ImageView imgThumbnail = (ImageView)v.findViewById(R.id.imgThumbnail);
			TextView imgDescription = (TextView)v.findViewById(R.id.txtDescription);
			
			imgThumbnail.setImageBitmap(img.imageBitmap);
			imgDescription.setText(img.imageDescription);
		}
		
		return v;
	}
}
