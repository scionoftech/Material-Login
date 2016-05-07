package scionoftech.loginapp;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class Login extends Fragment implements GoogleApiClient.OnConnectionFailedListener {


    public Login() {
        // Required empty public constructor
    }

    private boolean mIntentInProgress;
    //for knowing google + or facebook in activity result
    int i = 0;
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    //facebbok things
    private CallbackManager mCallbackManager;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;

    //initialize facebook components
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCallbackManager = CallbackManager.Factory.create();
        setupTokenTracker();
        setupProfileTracker();

        mTokenTracker.startTracking();
        mProfileTracker.startTracking();
    }

    String TransitionName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        if (getArguments() != null) {
            final Bundle bundle = getArguments();
            if (bundle != null) {
                TransitionName = bundle.getString("TRANS_NAME");
            }

            //set transition
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setTransitionName(TransitionName);
            }
        }

        //registration
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Login fragmentOne=new Login();

                Registration endFragment = new Registration();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setTransitionName("trans_clear");

                    setSharedElementReturnTransition(TransitionInflater.from(
                            getActivity()).inflateTransition(R.transition.change_image_trans));
                    setExitTransition(TransitionInflater.from(
                            getActivity()).inflateTransition(android.R.transition.fade));

                    endFragment.setSharedElementEnterTransition(TransitionInflater.from(
                            getActivity()).inflateTransition(R.transition.change_image_trans));
                    endFragment.setEnterTransition(TransitionInflater.from(
                            getActivity()).inflateTransition(android.R.transition.fade));

                    TransitionName = fab.getTransitionName();

                    //set transition
                    Bundle bundle = new Bundle();
                    bundle.putString("TRANS_NAME", TransitionName);
                    endFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, endFragment)
                            .addSharedElement(fab, TransitionName)
                            .commit();

                } else {

                }


            }
        });

        //-----------------------facebook
        mCallbackManager = CallbackManager.Factory.create();
        setupTokenTracker();
        setupProfileTracker();


        mTokenTracker.startTracking();
        mProfileTracker.startTracking();

        RelativeLayout facebook = (RelativeLayout) view.findViewById(R.id.facebook);
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupLoginButton();
            }
        });

        //-------------------------Google+

        RelativeLayout google = (RelativeLayout) view.findViewById(R.id.google);
        google.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                i = 1;//check facebook or google+
                SetUserPermission();
            }
        });

        //request email id
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addOnConnectionFailedListener(this)/*.addApi(Plus.API, Plus.PlusOptions.builder().build())*/
                .build();

        return view;
    }

    //*************************-------------------------------facebook api------------------------------*******************************//

    //facebook graph api get user
    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            Log.d("scion", "onSuccess");
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            // Application code
                            Log.d("data", object.toString());
                            try {
                                String milliseconds = getMilliseconds();
                                JSONObject profiledata = new JSONObject();
                                profiledata.put("social_id", object.get("id").toString());
                                if (!object.get("email").toString().equals(null)) {
                                    profiledata.put("email", object.get("email").toString());
                                } else {
                                    profiledata.put("email", "");
                                }

                                profiledata.put("password", "");
                                profiledata.put("name", object.get("name").toString());
                                profiledata.put("profile_pic", "https://graph.facebook.com/" + object.get("id").toString() + "/picture?type=large");
                                profiledata.put("gender", object.get("gender").toString());
                                profiledata.put("phone_number", "");
                                profiledata.put("city", "");
                                profiledata.put("country", "");
                                profiledata.put("loggedin_via", "facebook");
                                profiledata.put("last_seen", "");
                                profiledata.put("loggedin_time", milliseconds);
                                profiledata.put("terms_conditions", "yes");

                                Toast.makeText(getActivity(), "Hai " + object.get("name").toString(), Toast.LENGTH_LONG).show();
                                Log.d("facebook_data", profiledata.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.v("scion", object.toString());

                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday");
            request.setParameters(parameters);
            request.executeAsync();
           /* new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            Log.v("LoginActivity", response.toString());

                        }
                    }
            ).executeAsync();*/

        }

        @Override
        public void onCancel() {
            Log.d("scion", "onCancel");
        }

        @Override
        public void onError(FacebookException e) {
            Log.d("scion", "onError " + e);
        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // setupLoginButton();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //login result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (i == 0) {
            //facebook
            super.onActivityResult(requestCode, resultCode, data);
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            //google code
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    GoogleSignInAccount acct = result.getSignInAccount();
                    GetGdata(acct);
                }
            }
            i = 0;
        }
    }

    private void setupTokenTracker() {
        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.d("scion", "" + currentAccessToken);
            }
        };
    }

    private void setupProfileTracker() {
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                Log.d("scion", "" + currentProfile);

            }
        };
    }

    //facebook login request
    private void setupLoginButton() {
        LoginManager.getInstance().registerCallback(mCallbackManager, mFacebookCallback);
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile, email, user_birthday, user_friends"));
    }

    //*****************************************---------------------google+*********************************--------------------//
    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
        //facebook code
        mTokenTracker.stopTracking();
        mProfileTracker.stopTracking();
        LoginManager.getInstance().logOut();
        //google code
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        //stop auto manage
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(getActivity());
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

        if (!mIntentInProgress && arg0.hasResolution()) {
            try {
                mIntentInProgress = true;
                arg0.startResolutionForResult(getActivity(), RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent. Return to the
                // default
                // state and attempt to connect to get an updated
                // ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }

        }

        Log.e("scion", "User is onConnectionFailed!");

    }

    //********************************************** End of Google+ code *****************************************//
    public String getMilliseconds() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int second = c.get(Calendar.SECOND);
        int minute = c.get(Calendar.MINUTE);
        //12 hour format
        int hour = c.get(Calendar.HOUR);

        c.set(mYear, mMonth, mDay, hour, minute, second);

        String milli = String.valueOf(c.getTimeInMillis());

        return milli;
    }

    //************************************************ get google data *********************************************//
    public void GetGdata(GoogleSignInAccount googleSignInAccount) {

        try {
            String milliseconds = getMilliseconds();
            Uri personPhoto = googleSignInAccount.getPhotoUrl();
            String email = googleSignInAccount.getEmail();


            try {
                JSONObject profiledata = new JSONObject();
                profiledata.put("social_id", googleSignInAccount.getId());
                profiledata.put("email", email);
                profiledata.put("password", "");
                profiledata.put("name", googleSignInAccount.getDisplayName());
                profiledata.put("profile_pic", personPhoto);
                profiledata.put("gnder", "");
                profiledata.put("dob", "");
                profiledata.put("phone_number", "");
                profiledata.put("city", "");
                profiledata.put("country", "");
                profiledata.put("loggedin_via", "Google+");
                profiledata.put("last_seen", "");
                profiledata.put("loggedin_time", milliseconds);
                profiledata.put("terms_conditions", "yes");


                Toast.makeText(getActivity(), "Hai " + googleSignInAccount.getDisplayName(), Toast.LENGTH_LONG).show();

                Log.d("google", profiledata.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //set persmission in android 6.0
    public void SetUserPermission() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {Manifest.permission.GET_ACCOUNTS};


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage("you needs account access to login with Google+");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // sv();
                    Getpermission(permissions, 1);
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }

    }


    //send permission request
    private void Getpermission(String[] permissions, int PERMISSION_REQUEST) {
        ActivityCompat.requestPermissions(getActivity(), permissions,
                PERMISSION_REQUEST);
    }


    //grant permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == 0) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);

            } else {
                Toast.makeText(getActivity(), "you can't login with google+", Toast.LENGTH_LONG).show();
            }
        }
    }
}
