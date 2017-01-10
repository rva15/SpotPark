package com.example.android.sp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ruturaj on 10/14/16.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    static private ArrayList<FavoriteInfo> favoriteList;
    static String TAG="debugger",UID="";
    static public FragmentActivity activity;
    ContactViewHolder contactViewHolder;
    static RecyclerView recyclerView;
    View itemView;
    static DatabaseReference database;

    public ContactAdapter(ArrayList<FavoriteInfo> favoriteList,FragmentActivity activity,RecyclerView recyclerView,String UID) {
        this.favoriteList = favoriteList;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.UID = UID;

    }


    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        FavoriteInfo ci = favoriteList.get(i);
        this.contactViewHolder = contactViewHolder;
        contactViewHolder.spotname.setText(ci.name);
        contactViewHolder.spotimage.setImageBitmap(ci.spotimage);
    }

    public void notif(){
        this.notifyDataSetChanged();
    }


    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.favcard_layout, viewGroup, false);

        return new ContactViewHolder(itemView);
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView spotname;
        protected ImageView spotimage;


        public ContactViewHolder(View v) {
            super(v);
            spotname = (TextView) v.findViewById(R.id.spname);
            spotimage = (ImageView) v.findViewById(R.id.spimage);
            spotimage.setOnClickListener(this);
            Button button = (Button) v.findViewById(R.id.favcardbutton);
            button.setOnClickListener(this);
            ImageView deleteview = (ImageView) v.findViewById(R.id.deleteicon);
            ImageView editview = (ImageView) v.findViewById(R.id.editicon);
            deleteview.setOnClickListener(this);
            editview.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.favcardbutton) {
                int itemPosition = getAdapterPosition();
                double latitude = favoriteList.get(itemPosition).latitude;
                double longitude = favoriteList.get(itemPosition).longitude;
                String label = favoriteList.get(itemPosition).name;
                String uriBegin = "geo:" + latitude + "," + longitude;
                String query = latitude + "," + longitude + "(" + label + ")";
                String encodedQuery = Uri.encode(query);
                String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.getApplicationContext().startActivity(intent);

                return;

            }
            if (view.getId() == R.id.deleteicon) {
                deletedialog();
            }
            if (view.getId() == R.id.editicon) {
                editdialog();
                Log.d(TAG, "favoriteList " + Integer.toString(view.getId()));
            }

        }

        public void deletedialog() {
            Log.d(TAG, "entered deletedialog");
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Are you sure you want to delete this spot?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "deletedialog yes");
                    deletedata();

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "deletedialog no");
                    dialog.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void editdialog() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            LinearLayout layout = new LinearLayout(activity);
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(parms);
            TextView tv = new TextView(activity);
            tv.setText("Enter the new name tag for this spot");
            tv.setBackgroundResource(R.color.tab_background_unselected);
            tv.setGravity(Gravity.CENTER);
            final EditText et = new EditText(activity);
            layout.addView(tv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, activity.getResources().getDimensionPixelSize(R.dimen.text_view_height)));
            layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            alertDialogBuilder.setView(layout);

            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });

            // Setting Positive "OK" Button
            alertDialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG,"new title "+et.getText().toString());
                    database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
                    Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
                    childUpdates.put("/FavoriteKeys/" + UID + "/" + favoriteList.get(getAdapterPosition()).key+"/spotname", et.getText().toString());
                    database.updateChildren(childUpdates);
                    notif();
                    ((HomeScreenActivity) activity).getFavorite();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }



        public void deletedata() {
            Log.d(TAG,"deletedialog"+ favoriteList.get(getAdapterPosition()).key);
            database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
            Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
            childUpdates.put("/FavoriteKeys/" + UID + "/" + favoriteList.get(getAdapterPosition()).key, null);
            database.updateChildren(childUpdates);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference favoriteRef = storageRef.child(UID + "/Favorites/" + favoriteList.get(getAdapterPosition()).key + ".jpg");
            favoriteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG,"deletedialog success");
                    notif();
                    ((HomeScreenActivity) activity).getFavorite();
                }
            });

        }
    }
}

