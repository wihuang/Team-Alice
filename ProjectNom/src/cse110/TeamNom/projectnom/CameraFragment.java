package cse110.TeamNom.projectnom;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CameraFragment extends Fragment {

	// Final variables used to avoid MAGIC strings.
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int TAKE_PHOTO = 1;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private static final String ALBUM_NAME = "NOM";
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";

	// Misc variables
	private ImageButton bigBtn;
	private ImageButton picBtn;
	private TextView text;
	private ImageView mImageView;
	private Bitmap mImageBitmap;
	private EditText restaurant;
	private EditText caption;
	private Button subBut;
	private Context context;

	// String variables
	String parseRestaurant = "";
	String parseCaption = "";

	// The current Photo path
	private String mCurrentPhotoPath;
	private String pathtophoto;
	// Uri
	private Uri contentUri;
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	// Restart/pause strings
	private String restTmp;
	private String capTmp;
	private Drawable imageTmp;
	private String pathTmp;
	private boolean wasVisible = false;
	private boolean isRestart;

	// ---------------------------Camera Gallery
	// Methods-------------------------------------------

	/**
	 * Method that returns the photo gallery on the phone. Used to save
	 * photofile onto the phone. Used by: createImageFile
	 * 
	 * @return strageDir - album where picture is stored
	 */
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(ALBUM_NAME);
			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						return null;
					}
				}
			}

		} else {
		}

		return storageDir;
	}

	/**
	 * Method used to create the file with the image with date and jpeg format
	 * Calls: getAlbumDir Used by: setUpPhotoFile
	 * 
	 * @return imageF - image file with jpeg, timestamp, and album location
	 * @throws IOException
	 */
	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}

	/**
	 * sets up file path to photo Calls: createImageFile Used by:
	 * dispatchTakePictureIntent
	 * 
	 * @return returns image file
	 * @throws IOException
	 */
	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath(); // gets the path of the
													// photofile in the android
													// device
		return f;
	}

	/**
	 * sets and scales picture for imageview
	 */
	private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		mImageBitmap = bitmap;

		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(bitmap);
		mImageView.setVisibility(View.VISIBLE);
	}

	/**
	 * Uses intent to add the set up picture with photo path into the phone's
	 * photo gallery Used by: handlePhoto
	 */
	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		getActivity().sendBroadcast(mediaScanIntent);
	}

	/**
	 * sets path to the photo and saves the photo into the phone's gallery
	 * calls: setPic and galleryAddPic Used by: onActivityResult
	 */
	private void handlePhoto() {
		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			pathtophoto = mCurrentPhotoPath;
			mCurrentPhotoPath = null;
		}
	}

	// ------------------------Camera Access
	// Methods-------------------------------------------

	/**
	 * dispatches intent to the android camera in order to access it Calls:
	 * setUpPhotoFile Used by: onCreateView when take photo button is clicked
	 * 
	 * @param actionCode
	 *            what action user specified when taking picture (cancel, check,
	 *            undo)
	 */
	private void dispatchTakePictureIntent(int actionCode) {

		// dispatch intent to android for camera access
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (actionCode == TAKE_PHOTO) {
			File f = null;
			try {
				// set up photo
				f = setUpPhotoFile();
				// set path
				mCurrentPhotoPath = f.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
		}
		startActivityForResult(takePictureIntent, actionCode);
	}

	// on click for picture taking button
	Button.OnClickListener mTakePicOnClickListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(TAKE_PHOTO);
		}
	};

	/**
	 * method is called after photo is taken by user in android camera and sets
	 * gps calls setFragmentVisibility(), handlePhoto()
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TAKE_PHOTO && resultCode == -1) {

			// save location of picture, used in location based feed
			GPSFragment gps = new GPSFragment(getActivity());

			if (!gps.canGetLocation())
				gps.showSettingsAlert();

			setFragmentVisibility(0);

			handlePhoto();
		}
	}

	// -------------------------UI
	// Methods------------------------------------------

	/**
	 * Method sets up the whole UI when fragment is created Calls:
	 * setFragmentVisibility, mTakePicOnClickListener, isDeviceSupportCamera,
	 * onClickPicture, onCLickSubmit Used By: android system
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_camera, container,
				false);

		// set local variables to fragment part
		picBtn = (ImageButton) rootView.findViewById(R.id.btnCapturePicture);
		restaurant = (EditText) rootView.findViewById(R.id.RestaurantTitle);
		caption = (EditText) rootView.findViewById(R.id.pictureCaption);
		subBut = (Button) rootView.findViewById(R.id.submitButton);
		mImageView = (ImageView) rootView.findViewById(R.id.imageView1);
		bigBtn = (ImageButton) rootView.findViewById(R.id.initialButton);
		text = (TextView) rootView.findViewById(R.id.TakePictureText);
		
		// get context for toast
		context = getActivity().getApplicationContext();

		// if statement that restores user input on
		// fragment restart after process was killed
		if (isRestart) {
			restaurant.setText(restTmp);
			caption.setText(capTmp);
			if (imageTmp != null) {
				mImageView.setImageDrawable(imageTmp);
				mImageView.setImageBitmap(mImageBitmap);

			}
			if (!wasVisible) {
				setFragmentVisibility(1);
			} else {
				setFragmentVisibility(0);
			}
			mCurrentPhotoPath = pathTmp;
		} else {
			setFragmentVisibility(1);
		}

		/**
		 * initial camera button on click event
		 */
		bigBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickPicture(bigBtn);
			}
		});

		text.bringToFront();

		/**
		 * Capture image button click event
		 */
		picBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickPicture(picBtn);
			}
		});

		/**
		 * submission button on click event
		 */
		subBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickSubmit();
			}
		});

		// Checking camera availability
		if (!isDeviceSupportCamera()) {
			Toast.makeText(super.getActivity().getApplicationContext(),
					"Sorry! Your device doesn't support camera",
					Toast.LENGTH_LONG).show();
			// will close the app if the device does't have camera
			super.getActivity().finish();
		}
		return rootView;
	}

	/**
	 * Resets visibility on the fragment making it so the fragment looks brand
	 * new Calls: setFragmentVisibility
	 */
	private void resetFragment() {
		setFragmentVisibility(1);
		mImageView.setImageDrawable(null);
		restaurant.setText(null);
		caption.setText(null);
		isRestart = false;
	}

	/**
	 * Some lifecycle callbacks so that the image can survive orientation change
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY,
				(mImageBitmap != null));
	}

	/**
	 * Method that restores the state of the fragment after the user comes back
	 * after leaving, used to ensure no loss of user input
	 * 
	 * @param savedInstanceState
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		setFragmentVisibility(0);
	}

	/**
	 * method that saves params and user inputs when the fragment gets left or
	 * if the app is paused
	 */
	public void onPause() {
		super.onPause();
		restTmp = restaurant.getText().toString();
		capTmp = caption.getText().toString();
	}

	/**
	 * method that saves parameters and user inputs for when the fragment gets
	 * killed/stopped
	 */
	public void onStop() {
		isRestart = true;
		super.onStop();
		pathTmp = pathtophoto;
		restTmp = restaurant.getText().toString();
		capTmp = caption.getText().toString();
		imageTmp = mImageView.getDrawable();
		if (restaurant.getVisibility() == View.VISIBLE) {
			wasVisible = true;
		}
	}

	/**
	 * method that sets up the fragment after the fragment is stopped/killed and
	 * come back to
	 */
	public void onRestart() {
		restaurant.setText(restTmp);
		caption.setText(capTmp);
		mImageView.setImageDrawable(imageTmp);
		mImageView.setImageBitmap(mImageBitmap);
		setFragmentVisibility(0);
	}

	/**
	 * Method that sets up the fragment when the fragment is resumed after the
	 * user goes away and comes back
	 */
	public void onResume() {
		super.onResume();
	}

	// ------------------------------Parse
	// Methods---------------------------------------------

	/**
	 * Method that compresses a file given a path. Compresses file for parse
	 * Called by onCreateView after submit popup button is clickeed
	 * 
	 * @param path
	 * @throws Exception
	 */
	private void compressFile(String path) throws Exception {
		GPSFragment gps = new GPSFragment(getActivity());
		Bitmap bitmap;
		bitmap = BitmapFactory.decodeFile(path);

		if (bitmap == null) {
			return;
		}

		Bitmap bmpCompressed = Bitmap
				.createScaledBitmap(bitmap, 400, 400, true);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmpCompressed.compress(CompressFormat.JPEG, 100, bos);
		byte[] data = bos.toByteArray();

		parseRestaurant = restaurant.getText().toString();
		parseCaption = caption.getText().toString();
		
		// Makes a call to AppParseAccess to save the new picture with to Parse
		AppParseAccess.putMyPicture(data, AppFacebookAccess.getFacebookId(),
				parseCaption, parseRestaurant, gps.getLatitude(),
				gps.getLongitude());

	}

	// --------------------------------Helper Methods----------------------------------------

	/**
	 * Pretty straightforward, checks if device has camera functions or not Used
	 * by: onCreateView
	 * 
	 * @return bool whether phone has camera or not
	 */
	private boolean isDeviceSupportCamera() {
		if (super.getActivity().getApplicationContext().getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/**
	 * sets onclicklistener for button
	 * 
	 * @param btn
	 * @param onClickListener
	 * @param intentName
	 */
	private void setBtnListenerOrDisable(ImageButton btn,
			Button.OnClickListener onClickListener, String intentName) {

		// checks if intent is availabe for the camera button, if not makes the
		// button unclickable
		if (isIntentAvailable(super.getActivity().getApplicationContext(),
				intentName)) {
			btn.setOnClickListener(onClickListener);
		} else {
			btn.setClickable(false);
		}
	}

	/**
	 * method checks if intent is available for fragment Used by:
	 * setBtnListenerOrDisable
	 * 
	 * @param context
	 * @param action
	 * @return returns true if list.size > 0
	 */
	public static boolean isIntentAvailable(Context context, String action) {

		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	/**
	 * sets the buttons, texts, images visibility to visible or invisible 1 =
	 * invisible, 0 = visible Used by:
	 * 
	 * @param i
	 *            - int to check for invisible or visible
	 */
	private void setFragmentVisibility(int i) {
		if (i == 1) {
			restaurant.setVisibility(View.INVISIBLE);
			caption.setVisibility(View.INVISIBLE);
			subBut.setVisibility(View.INVISIBLE);
			mImageView.setVisibility(View.INVISIBLE);
			picBtn.setVisibility(View.INVISIBLE);
			bigBtn.setVisibility(View.VISIBLE);
			text.setVisibility(View.VISIBLE);
		} else {
			restaurant.setVisibility(View.VISIBLE);
			caption.setVisibility(View.VISIBLE);
			subBut.setVisibility(View.VISIBLE);
			mImageView.setVisibility(View.VISIBLE);
			picBtn.setVisibility(View.VISIBLE);
			bigBtn.setVisibility(View.INVISIBLE);
			text.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * handles onClick for take picture button Calls: dispatchTakePictureIntent
	 * Used by: onCreateView
	 */
	private void onClickPicture(ImageButton btn) {

		// sets button listener for the param image btn
		setBtnListenerOrDisable(btn, mTakePicOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

		// sends intent to method that accesses camera
		dispatchTakePictureIntent(TAKE_PHOTO);
	}

	/**
	 * method that reads the onclick of the submit button submits the photo to
	 * parse after prompting user Calls:compressFile Used By: onCreateView
	 */
	private void onClickSubmit() {

		// builder creates the popup box
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(false);
		builder.setMessage("Are you sure you want to upload?")
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {

									// check to see if restuarant box was left
									// empty
									String test = restaurant.getText()
											.toString();
									if (test.matches("")) {
										// if so, create a toast that asks for
										// the restuarant name
										Toast toast = Toast
												.makeText(
														context,
														"Please enter restaurant name ",
														Toast.LENGTH_LONG);
										toast.show();
									} else {
										// if not, send file to parse
										compressFile(pathtophoto);

										// create toast to show completion
										Toast toast = Toast.makeText(context,
												"File Uploaded Successfully",
												Toast.LENGTH_LONG);
										toast.show();

										// reset the ui of the fragment
										resetFragment();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		// show the builder
		builder.show();
	}

	/**
	 * method that switches fragment to main page
	 */
	public void switchToMain() {
		getActivity().getActionBar().setSelectedNavigationItem(0);
	}
}