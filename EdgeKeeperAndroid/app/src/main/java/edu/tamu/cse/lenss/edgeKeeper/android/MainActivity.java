package edu.tamu.cse.lenss.edgeKeeper.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.Context;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import edu.tamu.cse.lenss.edgeKeeper.client.EKClient;
import edu.tamu.cse.lenss.edgeKeeper.server.EKHandler;
import edu.tamu.cse.lenss.edgeKeeper.server.EdgeStatus;
import edu.tamu.cse.lenss.edgeKeeper.utils.EKProperties;
import edu.tamu.cse.lenss.edgeKeeper.utils.EKUtilsAndroid;


public class MainActivity extends AppCompatActivity {


    Context context;
    Logger logger = Logger.getLogger(this.getClass());

    public static GridView GV;

    static final int FILE_CHOOSER_CODE = 487;
    private EKService mEKService;
    private boolean mBound = false;
    private boolean SERVICE_STARTED = false;


    enum ServiceStatus {STARTED,CONNECTED,TERMINATED}
    ServiceStatus serviceStatus = ServiceStatus.TERMINATED;
    SharedPreferences sharedPreferences;

    // Create the service binder
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mEKService = ((EKService.LocalBinder) iBinder).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mEKService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();

        this.getActionBar();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        checkPermissions();

    }

    //initializes gridview.
    //must be called from onCreate()
    private void setupGridView() {

        //init gridview object pointing to the right resource at xml
        GV = findViewById(R.id.idGV);


        // Set Long-Clickable
        GV.setLongClickable(true);
        GV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @SuppressLint("NewApi")
            public boolean onItemLongClick(AdapterView<?> parent, View arg1,
                                           int position, long arg3) {
                Toast.makeText(getApplicationContext(), "Item Long click not implemented", Toast.LENGTH_SHORT).show();;

                return false;
            }
        });

        //Set On-Clicked
        GV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(), "Item click not implemented", Toast.LENGTH_SHORT).show();;
            }
        });

    }

    protected void onDestroy() {

        logger.info("Inside onDestroy function. isFinishing: "+isFinishing());
        Autostart.stopEKService(this.getApplicationContext());

        String enablePerpetualRunKey = context.getString(R.string.enable_perpetual_run);
        if( sharedPreferences.getBoolean(enablePerpetualRunKey, false)) {
            logger.info("Perpetual run is enabled. Calling Autostart to restart EKService");
            //Restart the Service
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(context.getString(R.string.restartservice));
            broadcastIntent.setClass(this, Autostart.class);
            this.sendBroadcast(broadcastIntent);
        }
        else
            logger.info("Perpetual run is disabled. Not starting service");

        super.onDestroy();

    }

    /**
     * This function initializes the App
     */
    protected void initializeApp(){

        // Configure the logger to store logs in file
        try {
            EKUtilsAndroid.initLoggerAndroid(context);
        } catch (IOException e) {
            logger.error(e);
        }

        logger.info("Initializing App main Activity");


        if( sharedPreferences.getString(EKProperties.p12Path, null) == null)
            showSetting();
        else if(Autostart.isEKServiceRunning(this)){
            logger.info(EKService.class.getSimpleName()+" is already running. So, not starting service");
            //return;
        } else {
            Autostart.startEKService(this.getApplicationContext());
        }

        //init gridivew
        setupGridView();

        //start thread that will periodically update gridview
        Thread GVupdater = new Thread(new UpdateGridView(getApplicationContext(), this, GV));
        GVupdater.start();

  }

    /*
        Now deal with the menus
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_setting:
                showSetting();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSetting() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }


    private static final int REQEUST_PERMISSION_GNSSERVICE = 22;

    private void checkPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            logger.warn("Permission not granted for Writing to external storage");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQEUST_PERMISSION_GNSSERVICE);
            }
        }
        else
            initializeApp();
   }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQEUST_PERMISSION_GNSSERVICE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logger.info("WRITE_EXTERNAL_STORAGE permission granted");
                    initializeApp();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                    logger.info("Permission not granted");
                    checkPermissions();
                }
        }
    }

    @Override
    public void onBackPressed(){
        logger.debug("Back button pressed. Not doing anything");
    }

    //a child clss that will run a thread that will periodically update gridview
    class UpdateGridView implements Runnable{

        Context context;
        Activity activity;
        GridView GV;

        public UpdateGridView(Context c, Activity activity, GridView gv){
            this.context = c;
            this.activity = activity;
            this.GV = gv;
        }

        @Override
        public void run() {
            System.out.println("xyz I should be printed only once!");
            try {

                ArrayList<GVItem> pinned;
                ArrayList<GVItem> unpinned;
                ArrayList<String> temp;
                ArrayList<GVItem> cloud;

                while (true) {

                    pinned = new ArrayList<>();
                    unpinned = new ArrayList<>();
                    temp = new ArrayList<>();
                    cloud = new ArrayList<>();

                    //HANDLE GNS/CLOUD INFORMATION HERE
                    if(GridViewStore.GNS_status!=null && GridViewStore.GNS_status.get()){

                        //cloud is always pinned
                        cloud.add(new GVItem(true, "Cloud", true, getApplicationContext(), findViewById(android.R.id.content).getRootView()));

                    }else{

                        //there was a valid item found in Queue
                        cloud.add(new GVItem(false, "Cloud", true, getApplicationContext(), findViewById(android.R.id.content).getRootView()));
                    }


                    //we fetch edge replica and topology information only if EKClient is connected
                    if(GridViewStore.EKClient_status!=null && GridViewStore.EKClient_status.get()) {

                        //get my name from EKClient
                        if(GridViewStore.myName==null) {
                            GridViewStore.myName = EKClient.getOwnAccountName();
                        }

                        //HANDLE EDGE REPLICA STATUS INFORMATION HERE
                        try {
                            EdgeStatus status = null;
                            status = EKHandler.edgeStatus;
                            if (status != null) {

                                //get all guids which formed this edge
                                Set<String> replicaGUIDs = status.replicaMap.keySet();

                                System.out.println("xyz replica info (guid): " + replicaGUIDs);

                                //iterate over each guid
                                for (String guid : replicaGUIDs) {

                                    //get name for each GUID
                                    String name = EKClient.getAccountNamebyGUID(guid);

                                    if (name != null) {

                                        //trim name
                                        name = name.substring(0, name.indexOf("."));

                                        //check if this name has already been handled
                                        if (!temp.contains(name)) {

                                            //add this name to res
                                            if (GridViewStore.pinnedItems.contains(name)) {
                                                pinned.add(new GVItem(true, name, true, getApplicationContext(), findViewById(android.R.id.content).getRootView()));
                                            } else {
                                                unpinned.add(new GVItem(true, name, false, getApplicationContext(), findViewById(android.R.id.content).getRootView()));
                                            }

                                            //add name into temp list
                                            temp.add(name);

                                        }
                                    } else {
                                        System.out.println("XYZ guid to name conversion failed!");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("XYZ exception is processing edge replica information");
                            e.printStackTrace();
                        }


                        //HANDLE TOPOLOGY INFORMATION HERE
                        try {
                            Set<String> ips = null;
                            ips = EKClient.getNetworkInfo().getAllIPs();
                            if (ips != null) {
                                //iterate over each IP
                                for (String ip : ips) {

                                    System.out.println("XYZ network info (ip): " + ips);

                                    //get all names for this IP
                                    List<String> names = EKClient.getAccountNamebyIP(ip);

                                    //check if not null or empty
                                    if (names != null && names.size() > 0) {

                                        //trim name
                                        String name = names.get(0).substring(0, names.get(0).indexOf("."));

                                        //check if this name has already been handled
                                        if (!temp.contains(name)) {

                                            //add this name to res
                                            if (GridViewStore.pinnedItems.contains(name)) {
                                                pinned.add(new GVItem(true, name, true, getApplicationContext(), findViewById(android.R.id.content).getRootView()));
                                            } else {
                                                unpinned.add(new GVItem(true, name, false, getApplicationContext(), findViewById(android.R.id.content).getRootView()));
                                            }

                                            //add name into temp list
                                            temp.add(name);
                                        }
                                    } else {
                                        System.out.println("XYZ ip to name conversion failed!");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("XYZ exception is processing topology information");
                            e.printStackTrace();
                        }
                    }

                    //check cloud for last time
                    if(cloud==null || (cloud!=null && cloud.size()==0)){
                        cloud.add(new GVItem(GridViewStore.cloudStatusCache, "Cloud", true, getApplicationContext(), findViewById(android.R.id.content).getRootView()));
                    }

                    //cloud = cloud + pinned + pinnedItems + unpinned
                    //add pinned item(CONNECTED) into cloud list
                    cloud.addAll(pinned);

                    //add pinned items(DISCONNECTED) into cloud list
                    for(String pinnedDisItem: GridViewStore.pinnedItems){
                        if(!temp.contains(pinnedDisItem)){
                            cloud.add(new GVItem(false, pinnedDisItem, true, getApplicationContext(), findViewById(android.R.id.content).getRootView()));
                            temp.add(pinnedDisItem);
                        }
                    }

                    //add unpinned (CONNECTED) items into cloud list
                    cloud.addAll(unpinned);

                    //delete myself
                    if(GridViewStore.myName!=null) {
                        for (int i = 0; i < cloud.size(); i++) {
                            if (cloud.get(i).getNAME().equals(GridViewStore.myName.substring(0, GridViewStore.myName.indexOf(".")))) {
                                cloud.remove(i);
                            }
                        }
                    }

                    //update UI
                    //run on UI thread aka mainActivity in this case
                    GVAdapter adapter = new GVAdapter(context, cloud);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set adapter for gridview
                            GV.setAdapter(adapter);
                        }
                    });


                    //sleep
                    try {
                        int updateInterval = EKHandler.getEKProperties().getInteger(EKProperties.topoInterval);
                        Thread.sleep(updateInterval);
                    }catch (Exception e ){
                        e.printStackTrace();
                        Thread.sleep(10000);
                    }
                }


            }catch (Exception e){
                System.out.println("XYZ EXCEPTION in mainActivity" + e);
                e.printStackTrace();
            }
        }
    }
}
