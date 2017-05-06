package com.app.android.sp;
//All imports
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 1/12/17.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    //Variable Declarations
    private ArrayList<String> keys = new ArrayList<String>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private ArrayList<HistoryPlace> historyplace = new ArrayList<HistoryPlace>();
    static String TAG = "debugger", UID = "";
    private HistoryFragment historyFragment;
    private static FragmentActivity activity;
    private HistoryViewHolder historyViewHolder;
    private static RecyclerView recyclerView;
    private View itemView;
    private Context context;
    private int max;

    //constructor
    public HistoryAdapter(ArrayList historyplace, ArrayList keys, ArrayList bitmaps, FragmentActivity activity, HistoryFragment historyFragment, RecyclerView recyclerView, String UID, Context context,int max) {

        this.historyplace = historyplace;
        this.keys = keys;
        this.bitmaps = bitmaps;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.UID = UID;
        this.context = context;
        this.historyFragment = historyFragment;
        this.max = max;
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder historyViewHolder, int i) {
        this.historyViewHolder = historyViewHolder;
        historyViewHolder.date.setText(historyplace.get(max-1-i).getdate());      //set the ArrayList elements to the views
        historyViewHolder.time.setText(historyplace.get(max-1-i).gettime());
        historyViewHolder.hisspotimage.setImageBitmap(bitmaps.get(max-1-i));
        if(historyplace.get(max-1-i).getisfavorite()==1){
            historyViewHolder.addtofavorites.setChecked(true);
            historyViewHolder.addtofavorites.setEnabled(false);
        }
    }

    public void refresh() {
        this.notifyDataSetChanged();
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.historycard_layout, viewGroup, false);

        return new HistoryViewHolder(itemView);
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView date, time;
        protected ImageView hisspotimage;
        protected CheckBox addtofavorites;

        public HistoryViewHolder(View v) {
            super(v);
            date = (TextView) v.findViewById(R.id.hisdate);
            time = (TextView) v.findViewById(R.id.histime);
            addtofavorites = (CheckBox) v.findViewById(R.id.addtofavorites);
            hisspotimage = (ImageView) v.findViewById(R.id.hisspotimage);
            Button button = (Button) v.findViewById(R.id.hiscardnavigate);
            button.setOnClickListener(this);
            addtofavorites.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.addtofavorites) {
                if (addtofavorites.isChecked()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    TextView tv = new TextView(context);
                    tv.setText("Enter a suitable name for this spot");
                    tv.setGravity(Gravity.CENTER);
                    final int version = Build.VERSION.SDK_INT;
                    if (version >= 23) {
                        tv.setBackgroundColor(ContextCompat.getColor(context,R.color.tab_background_selected));
                    } else {
                        tv.setBackgroundColor(context.getResources().getColor(R.color.tab_background_selected));
                    }
                    final EditText et = new EditText(context);
                    alertDialogBuilder.setView(et);
                    alertDialogBuilder.setCustomTitle(tv);
                    alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                            addtofavorites.setChecked(false);
                        }
                    });

                    // Setting Positive "OK" Button
                    alertDialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            favorite(max-1-getAdapterPosition(),et.getText().toString());
                            addtofavorites.setEnabled(false);    //disable the checkbox soon as it is clicked
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }
            }
            if(view.getId()==R.id.hiscardnavigate){     //send intent to google maps api
                double lat = historyplace.get(getAdapterPosition()).getplatitude();
                double lon = historyplace.get(getAdapterPosition()).getplongitude();
                String label = "From Your History";
                String uriBegin = "geo:" + lat + "," + lon;
                String query = lat + "," + lon + "(" + label + ")";
                String encodedQuery = Uri.encode(query);
                String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.getApplicationContext().startActivity(intent);
            }
        }

        private void favorite(int position,String spotname){

            Double latitude = historyplace.get(position).getplatitude();
            Double longitude = historyplace.get(position).getplongitude();
            Bitmap bitmap   = bitmaps.get(position);
            String key      = keys.get(position);


            DatabaseReference database;
            database = FirebaseDatabase.getInstance().getReference();

            FavoritePlace favoritePlace = new FavoritePlace(latitude,longitude,spotname);
            Map<String, Object> favoriteMap = favoritePlace.toMap();
            Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
            childUpdates.put("/FavoriteKeys/"+UID+"/"+key, favoriteMap);
            childUpdates.put("/HistoryKeys/"+UID+"/"+key+"/isfavorite",1);
            database.updateChildren(childUpdates);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datum = baos.toByteArray();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference favoriteRef = storageRef.child(UID+"/Favorites/"+key+".jpg");

            UploadTask uploadTask = favoriteRef.putBytes(datum);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                }
            });

            Toast.makeText(context,"Added this spot to Favorites",Toast.LENGTH_SHORT).show();

        }


    }
}
