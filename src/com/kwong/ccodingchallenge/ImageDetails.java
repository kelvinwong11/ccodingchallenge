package com.kwong.ccodingchallenge;

import android.graphics.Bitmap;

public class ImageDetails {
	public String id;
	public String serverID;
	public String farmID;
	public String secret;
	public String imageDescription;
	public Bitmap imageBitmap;
	
	public ImageDetails() {
		super();
	}
	
	public ImageDetails(String id, String secret, String serverID, String farmID,
			String description) {
		super();
		this.id = id;
		this.secret = secret;
		this.serverID = serverID;
		this.farmID = farmID;
		imageDescription = description;
	}
	
	public void setImageBitmap (Bitmap image) {
		imageBitmap = image;
	}
}
