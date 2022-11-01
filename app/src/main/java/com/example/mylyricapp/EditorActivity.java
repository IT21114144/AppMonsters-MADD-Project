package com.example.mylyricapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    EditText mNameEditText, mArtistEditText, mGenresEditText ;
    private Uri mPhotoUri;
    private Uri mCurrentSongUri;

    ImageView mPhoto;
    private boolean mSongHasChanged = false;
    Spinner mSpinner;
    public static final int LOADER = 0;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mSongHasChanged = true;
            return false;
        }
    };

    boolean hasAllRequiredValues = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentSongUri = intent.getData();

        mNameEditText = findViewById(R.id.nameEditText);
        mArtistEditText = findViewById(R.id.artistEditText);
        mGenresEditText = findViewById(R.id.genresEditText);

        mPhoto = findViewById(R.id.song_image);


        if (mCurrentSongUri == null) {
            mPhoto.setImageResource(R.drawable.photo);
            setTitle("Add a Song");
            // we want to hide delete menu when we are adding a new contact
            invalidateOptionsMenu();

        } else {
            setTitle("Edit a Song");
            getLoaderManager().initLoader(LOADER, null, this);

        }

        mNameEditText.setOnTouchListener(mOnTouchListener);
        mArtistEditText.setOnTouchListener(mOnTouchListener);
        mGenresEditText.setOnTouchListener(mOnTouchListener);
        mPhoto.setOnTouchListener(mOnTouchListener);


        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mSongHasChanged = true;
            }
        });




    }




    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mPhotoUri = data.getData();
                mPhoto.setImageURI(mPhotoUri);
                mPhoto.invalidate();
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menueditor, menu);
        return true;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // because we want to hide delete option when we are adding a new contact
        super.onPrepareOptionsMenu(menu);
        if (mCurrentSongUri == null) {
            MenuItem item = (MenuItem) menu.findItem(R.id.delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                saveSong();
                if (hasAllRequiredValues == true) {
                    finish();
                }
                return true;

            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!mSongHasChanged) {

                    // we will be displayed a dialog asking us to discard or keeping editing when we press back in case
                    // we have not finished filling up some field

                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;

                }

                DialogInterface.OnClickListener discardButton = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);

                    }
                };
                showUnsavedChangesDialog(discardButton);
                return true;



        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveSong() {
        // last step of this activity we have to create save song method
        String name = mNameEditText.getText().toString().trim();
        String artist = mArtistEditText.getText().toString().trim();
        String genres = mGenresEditText.getText().toString().trim();

        if (mCurrentSongUri == null && TextUtils.isEmpty(name)
                && TextUtils.isEmpty(artist) && TextUtils.isEmpty(genres) && mPhotoUri == null) {
            hasAllRequiredValues = true;
            return hasAllRequiredValues;
        }
        ContentValues values = new ContentValues();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is Required", Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(Contract.ContactEntry.COLUMN_NAME, name);
        }

        if (TextUtils.isEmpty(artist)) {
            Toast.makeText(this, "Artist Name is Required", Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(Contract.ContactEntry.COLUMN_ARTIST, artist);
        }

        if (TextUtils.isEmpty(genres)) {
            Toast.makeText(this, "Genres is Required", Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(Contract.ContactEntry.COLUMN_TYPEOFGENRES, genres);
        }
        // optional values
        values.put(Contract.ContactEntry.COLUMN_PICTURE, mPhotoUri.toString());

        if (mCurrentSongUri == null) {

            Uri newUri = getContentResolver().insert(Contract.ContactEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Error with Saving", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Success with Saving", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentSongUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Error  with Update", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Success  with Update", Toast.LENGTH_SHORT).show();
            }

        }

        hasAllRequiredValues = true;

        return hasAllRequiredValues;




    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {Contract.ContactEntry._ID,
                Contract.ContactEntry.COLUMN_NAME,
                Contract.ContactEntry.COLUMN_ARTIST,
                Contract.ContactEntry.COLUMN_PICTURE,
                Contract.ContactEntry.COLUMN_TYPEOFGENRES,

        };

        return new CursorLoader(this, mCurrentSongUri,
                projection, null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // getting position of each column
            int name = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_NAME);
            int artist = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_ARTIST);
            int genres = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_TYPEOFGENRES);


            int picture = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_PICTURE);

            String songname = cursor.getString(name);
            String artistname = cursor.getString(artist);
            String typeofgenres = cursor.getString(genres);

            String songpicture = cursor.getString(picture);

            mPhotoUri = Uri.parse(songpicture);

            mArtistEditText.setText(artistname);
            mNameEditText.setText(songname);
            mGenresEditText.setText(typeofgenres);
            mPhoto.setImageURI(mPhotoUri);



        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mArtistEditText.setText("");
        mNameEditText.setText("");
        mGenresEditText.setText("");

        mPhoto.setImageResource(R.drawable.photo);

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentSongUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentSongUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mSongHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}

