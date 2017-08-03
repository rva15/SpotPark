package com.application.android.sp;
//All imports
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 11/22/16.
 */
public class CRAdapter extends RecyclerView.Adapter<CRAdapter.ContactViewHolder> {
    //Variable Declarations
    static private ArrayList<Bitmap> crimage;
    static private ArrayList<String> crkey;
    static private ArrayList<ReportedTimes> crtimes;
    private static String TAG="debugger",UID="";
    static private FragmentActivity activity;
    private ContactViewHolder contactViewHolder;
    private static RecyclerView recyclerView;
    private View itemView;
    private static DatabaseReference database;

    public CRAdapter(ArrayList crimage,ArrayList crtimes,ArrayList crkey,FragmentActivity activity,RecyclerView recyclerView,String UID) {
        this.crimage = crimage;
        this.crtimes = crtimes;
        this.crkey   = crkey;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.UID = UID;

    }


    @Override
    public int getItemCount() {
        return crkey.size();
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        this.contactViewHolder = contactViewHolder;
        contactViewHolder.spotname.setText(crtimes.get(i).getdescription());
        if(crtimes.get(i).getverification()<2) {
            contactViewHolder.category.setImageResource(R.drawable.uver);
            contactViewHolder.keyicon.setVisibility(View.GONE);
            contactViewHolder.points.setVisibility(View.GONE);
        }
        else{
            contactViewHolder.category.setImageResource(R.drawable.ver);
            contactViewHolder.keyicon.setVisibility(View.VISIBLE);
            contactViewHolder.points.setVisibility(View.VISIBLE);
            contactViewHolder.points.setText("5");
        }
        contactViewHolder.spotimage.setImageBitmap(crimage.get(i));
    }

    public void notif(){
        this.notifyDataSetChanged();
    }


    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.contrepcard_layout, viewGroup, false);

        return new ContactViewHolder(itemView);
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView spotname,points;
        protected ImageView spotimage,keyicon;
        protected ImageView category;


        public ContactViewHolder(View v) {
            super(v);
            spotname = (TextView) v.findViewById(R.id.contrepname);
            spotimage = (ImageView) v.findViewById(R.id.contrepimage);
            category = (ImageView) v.findViewById(R.id.crstatus);
            points = (TextView) v.findViewById(R.id.points);
            keyicon = (ImageView) v.findViewById(R.id.keyicon);
            spotimage.setOnClickListener(this);
            ImageView deleteview = (ImageView) v.findViewById(R.id.crdeleteicon);
            ImageView editview = (ImageView) v.findViewById(R.id.crediticon);
            deleteview.setOnClickListener(this);
            editview.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.crediticon) {
                int itemPosition = getAdapterPosition();
                Bundle data = new Bundle();
                data.putString("UID",UID);
                data.putString("key", crkey.get(itemPosition));
                data.putParcelable("reportedtimes",crtimes.get(itemPosition));
                ReportedEdit reportedEdit = new ReportedEdit();
                reportedEdit.setArguments(data);
                android.support.v4.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, reportedEdit);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                return;

            }
            if (view.getId() == R.id.crdeleteicon) {
                deletedialog();
            }

        }

        public void deletedialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Are you sure you want to delete this reported spot?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    deletedata();

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

        /*public void editdialog() {
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
        }*/



        public void deletedata() {
            database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
            Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
            childUpdates.put("/ReportedDetails/" + crtimes.get(getAdapterPosition()).getlatlngcode()+ "/" + crkey.get(getAdapterPosition()), null);
            childUpdates.put("/ReportedTimes/" + UID + "/" + crkey.get(getAdapterPosition()), null);
            database.updateChildren(childUpdates);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference favoriteRef = storageRef.child(UID + "/Reported/" + crkey.get(getAdapterPosition()) + ".jpg");
            favoriteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    notif();
                    ((HomeScreenActivity) activity).getContri();
                }
            });

        }
    }
}
