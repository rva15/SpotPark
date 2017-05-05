package com.app.android.sp;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ruturaj on 4/15/17.
 */

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> {


    //Variable Declarations
    private ArrayList<Places> placesArrayList = new ArrayList<Places>();
    private ArrayList<String> placesKeysList = new ArrayList<String>();
    private Map deletekeys = new HashMap();
    private Map deleteplaces = new HashMap();
    static String TAG = "debugger", UID = "",type;
    private PlacesViewHolder placesViewHolder;
    private static RecyclerView recyclerView;
    private View itemView;
    private ImageView deleteicon;
    private DatabaseReference database;
    private Context context;

    //constructor
    public PlacesAdapter(ArrayList placesArrayList, ArrayList placesKeysList, String UID, RecyclerView recyclerView, ImageView deleteicon,String type,Context context) {

        this.placesArrayList = placesArrayList;
        this.placesKeysList  = placesKeysList;
        this.deleteicon = deleteicon;
        this.recyclerView = recyclerView;
        this.UID = UID;
        this.type = type;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return placesArrayList.size();
    }

    @Override
    public void onBindViewHolder(PlacesViewHolder placesViewHolder, int i) {
        this.placesViewHolder = placesViewHolder;
        placesViewHolder.placename.setText(placesArrayList.get(i).getplacename());      //set the ArrayList elements to the views
        placesViewHolder.selectplace.setChecked(false);

    }

    public void refresh() {
        this.notifyDataSetChanged();
    }

    @Override
    public PlacesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.placescard_layout, viewGroup, false);

        return new PlacesViewHolder(itemView);
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {

        protected TextView placename;
        protected CheckBox selectplace;

        public PlacesViewHolder(View v) {
            super(v);
            placename = (TextView) v.findViewById(R.id.placename);
            selectplace = (CheckBox) v.findViewById(R.id.selectplace);
            selectplace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){ //add the place to the list of places to delete
                        deletekeys.put(getAdapterPosition(),placesKeysList.get(getAdapterPosition()));
                        deleteplaces.put(getAdapterPosition(),placesArrayList.get(getAdapterPosition()));
                        deleteicon.setVisibility(View.VISIBLE);
                    }
                    else{ //remove the place from the list
                        deletekeys.remove(getAdapterPosition());
                        deleteplaces.remove(getAdapterPosition());
                        if(deletekeys.size()==0){
                            deleteicon.setVisibility(View.GONE);
                        }
                    }
                }
            });

            deleteicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //get confirmation from user
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Delete selected places from this list?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database = FirebaseDatabase.getInstance().getReference();
                            //remove selected places using an iterator
                            Iterator it = deletekeys.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry)it.next();
                                System.out.println(pair.getKey() + " = " + pair.getValue());
                                Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
                                childUpdates.put("/ST"+type+"/"+UID+"/"+pair.getValue(), null);
                                database.updateChildren(childUpdates);
                                placesArrayList.remove(deleteplaces.get(pair.getKey()));
                                placesKeysList.remove(pair.getValue());
                                it.remove();    // avoids a ConcurrentModificationException
                            }
                            deletekeys.clear();
                            deleteplaces.clear();
                            refresh();


                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

        }

    }


}
