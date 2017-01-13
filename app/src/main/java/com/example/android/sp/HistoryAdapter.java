package com.example.android.sp;
//All imports
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

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

    //constructor
    public HistoryAdapter(ArrayList historyplace, ArrayList keys, ArrayList bitmaps, FragmentActivity activity,HistoryFragment historyFragment, RecyclerView recyclerView, String UID) {

        this.historyplace = historyplace;
        this.keys = keys;
        this.bitmaps = bitmaps;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.UID = UID;
        this.historyFragment = historyFragment;
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder historyViewHolder, int i) {
        this.historyViewHolder = historyViewHolder;
        historyViewHolder.date.setText(historyplace.get(i).getdate());      //set the ArrayList elements to the views
        historyViewHolder.time.setText(historyplace.get(i).gettime());
        historyViewHolder.hisspotimage.setImageBitmap(bitmaps.get(i));
        if(historyplace.get(i).getisfavorite()==1){
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
                    showNamesDialog();
                    addtofavorites.setEnabled(false);    //disable the checkbox soon as it is clicked
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

        private void showNamesDialog() {
            // Create an instance of the dialog fragment and show it
            Bundle args = new Bundle();
            args.putDouble("latitude",historyplace.get(getAdapterPosition()).getplatitude());
            args.putDouble("longitude",historyplace.get(getAdapterPosition()).getplongitude());
            args.putParcelable("bitmap",bitmaps.get(getAdapterPosition()));
            args.putString("key",keys.get(getAdapterPosition()));
            android.support.v4.app.DialogFragment dialog = new NamesDialog();
            dialog.setArguments(args);
            dialog.setTargetFragment(historyFragment, 2);       //set target fragment to history fragment

            dialog.show(activity.getSupportFragmentManager(),"Names fragment");
        }


    }
}
