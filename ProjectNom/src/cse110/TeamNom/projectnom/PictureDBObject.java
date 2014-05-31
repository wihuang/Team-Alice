package cse110.TeamNom.projectnom;

import java.util.Date;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class PictureDBObject {
	String image_id;
	byte[] Food_photo;
	int Like;
	int Bookmark;
	boolean report_image;
	String FACEBOOK_ID;
	String FACEBOOK_NAME;
	String Food_name;
	String Restaurant_Id;
	double Latitude;
	double Longitude;
	Date createdAt;
	Date updatedAt;
	
	public PictureDBObject(ParseObject parse) {
		//Store picture
		ParseFile pf;
		pf = (ParseFile) parse.get("Food_photo");
		
		try {
			setPicture(pf.getData());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//Store image id
		setImageID(parse.getObjectId());
		
		//Store facebook name
		setFacebookName(parse.getString("FACEBOOK_NAME"));
		
		//Store facebook id
		setFacebookID(parse.getString("FACEBOOK_ID"));
		
		//Store like
		setLikeCount(parse.getInt("Like"));

		//Store bookmarks
		setBookmarkCount(parse.getInt("Bookmark"));
		
		//Store image reported status
		setImageReportStatus(parse.getBoolean("report_image"));
		
		//Store food name
		setRestID(parse.getString("Restaurant_Id"));
		
		//Store latitude
		setLatitude(parse.getDouble("Latitude"));
		
		//Store longitude
		setLongitude(parse.getDouble("Longitude"));
		
		//Store created at date
		setCreatedDate(parse.getCreatedAt());
		
		//Store updated at date
		setUpdatedDate(parse.getUpdatedAt());
	}
	
	public void setFacebookName(String name) {
		FACEBOOK_NAME = name;
	}
	
	public String getFacebookName() {
		return FACEBOOK_NAME;
	}
	
	public void setImageID(String id) {
		image_id = id;
	}
	
	public String getImageID() {
		return image_id;
	}
	
	public void setFacebookID(String id) {
		FACEBOOK_ID = id;
	}
	
	public String getFacebookID() {
		return FACEBOOK_ID;
	}
	
	public void setPicture(byte[] picture) {
		Food_photo = picture.clone();
	}

	public byte[] getPicture() {
		return Food_photo;
	}

	public void setLikeCount(int count) {
		Like = count;
	}

	public int getLikeCount() {
		return Like;
	}

	public void setBookmarkCount(int count) {
		Bookmark = count;
	}

	public int getBookmarkCount() {
		return Bookmark;
	}

	public void reportImage() {
		report_image = true;
	}

	public void unreportImage() {
		report_image = false;
	}

	public void setImageReportStatus(boolean status) {
		report_image = status;
	}
	
	public boolean getImageReportStatus() {
		return report_image;
	}

	public void setRestID(String caption) {
		Food_name = caption;
	}

	public String getRestID() {
		return Food_name;
	}
	
	public void setLatitude(double lat) {
		Latitude = lat;
	}
	
	public double getLatitude() {
		return Latitude;
	}
	
	public void setLongitude(double longit) {
		Longitude = longit;
	}
	
	public double getLongitude() {
		return Longitude;
	}
	
	public void setCreatedDate(Date created) {
		createdAt = created;
	}
	
	public Date getCreatedDate() {
		return createdAt;
	}
	
	public void setUpdatedDate(Date updated) {
		updatedAt = updated;
	}
	
	public Date getUpdatedDate() {
		return updatedAt;
	}
}

