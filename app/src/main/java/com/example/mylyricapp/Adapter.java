package com.example.mylyricapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class Adapter extends CursorAdapter {


    public Adapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listitem, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameView, artistView, genresView ;
        ImageView mSongImageView;

        nameView = view.findViewById(R.id.textName);
        artistView = view.findViewById(R.id.textArtistName);
        genresView = view.findViewById(R.id.textTypeofGenres);

        mSongImageView = view.findViewById(R.id.imageSong);
        /// geting position of views
        int name = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_NAME);
        int artist = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_ARTIST);
        int genres = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_TYPEOFGENRES);

        int picture = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_PICTURE);

        String songname = cursor.getString(name);
        String artistname = cursor.getString(artist);
        String songpicture = cursor.getString(picture);
        String songgenres = cursor.getString(genres);

        Uri imageUri = Uri.parse(songpicture);

        nameView.setText(songname);
        artistView.setText(artistname);
        genresView.setText(songgenres);



        mSongImageView.setImageURI(imageUri);



    }
}
