package com.example.android.sp;
// All imports
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by ruturaj on 12/15/16.
 */
public class MainmenuAdapter extends RecyclerView.Adapter<MainmenuAdapter.ViewHolder> {

    // Variable Declarations
    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    private static final int TYPE_ITEM = 1;    // IF the view under inflation and population is header or Item
    private String mNavTitles[];               // String Array to store the passed titles Value from HomeScreenActivity.java
    private int mIcons[];                      // Int Array to store the passed icons resource value from HomeScreenActivity.java
    private static String TAG="debugger";
    private String UID;
    public static Activity homeactivity;
    public static DrawerLayout drawer;
    private MainmenuAdapter.ViewHolder currholder;



    // Creating a ViewHolder which extends the RecyclerView View Holder
    // ViewHolder are used to to store the inflated views in order to recycle them

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        int Holderid;
        static String TAG= "debugger";
        TextView textView;
        ImageView imageView;
        ImageView profile;
        TextView Name;
        TextView email;


        public ViewHolder(View itemView,int ViewType) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);


            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created

            if(ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText);  // Creating TextView object with the id of textView from item_row.xml
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);// Creating ImageView object with the id of ImageView from item_row.xml
                Holderid = 1;                                               // setting holder id as 1 as the object being populated are of type item row
                textView.setOnClickListener(this);

            }
            else{
                Name = (TextView) itemView.findViewById(R.id.name);         // Creating Text View object from header.xml for name
                email = (TextView) itemView.findViewById(R.id.email);       // Creating Text View object from header.xml for email
                profile = (ImageView) itemView.findViewById(R.id.circleView);// Creating Image view object from header.xml for profile pic
                Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
            }
        }

        @Override
        public void onClick(View view) {

            TextView tv = (TextView) view;
            Log.d(TAG,"tv is "+tv.getText());
            String option = (String)tv.getText();
            HomeScreenActivity homeScreenActivity = (HomeScreenActivity) homeactivity;
            if(option.equals("Home")){
                homeScreenActivity.getHome();
                drawer.closeDrawers();
            }
            if(option.equals("History")){
                homeScreenActivity.getHistory();
                drawer.closeDrawers();
            }
            if(option.equals("Favorites")){
                homeScreenActivity.getFavorite();
                drawer.closeDrawers();
            }
            if(option.equals("Contributions")){
                homeScreenActivity.getContri();
                drawer.closeDrawers();
            }
            if(option.equals("Settings")){
                homeScreenActivity.getSettings();
                drawer.closeDrawers();
            }
            if(option.equals("Logout")){
                homeScreenActivity.backToLogin();
                drawer.closeDrawers();
            }
        }

    }



    MainmenuAdapter(String Titles[], int Icons[], Activity activity, DrawerLayout drawerLayout, String uid){ // MainmenuAdapter Constructor with titles and icons parameter

        mNavTitles = Titles;
        mIcons = Icons;
        homeactivity = activity;
        drawer = drawerLayout;
        UID = uid;
    }



    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder

    @Override
    public MainmenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row,parent,false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder

        } else if (viewType == TYPE_HEADER) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header,parent,false); //Inflating the layout

            ViewHolder vhHeader = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created


        }
        return null;

    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(MainmenuAdapter.ViewHolder holder, int position) {

        if(holder.Holderid ==1) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
            holder.imageView.setImageResource(mIcons[position -1]);// Settimg the image with array of our icons
        }
        else{

            currholder = holder;
            getbasicinfo();       //get user's name and email
            getdp(holder);        //get user's profile pic
        }
    }

    @Override
    public int getItemCount() {
        return mNavTitles.length+1; // the number of items in the list will be +1 the titles including the header view.
    }


    // Witht the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    //------------------Helper Functions-------------------------//

    public void getbasicinfo(){
        DatabaseReference database;
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).addValueEventListener(listener1);
    }

    ValueEventListener listener1 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
            currholder.Name.setText(userDetails.getfirstname() + " "+userDetails.getlastname());
            currholder.email.setText(userDetails.getemail());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    public void getdp(final MainmenuAdapter.ViewHolder holdertemp){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
        StorageReference islandRef = storageRef.child(UID+"/Profile/dp.jpg");
        Log.d(TAG,"looking up "+islandRef.toString());
        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "download success");
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holdertemp.profile.setImageBitmap(getCroppedBitmap(bmp,holdertemp));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "download failed");
                holdertemp.profile.setImageResource(R.drawable.user); //set the default profile pic
            }
        });
    }

    // function the crops the profile pic into a circle
    public Bitmap getCroppedBitmap(Bitmap bitmap,final MainmenuAdapter.ViewHolder holdertemp2) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getHeight() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


}
