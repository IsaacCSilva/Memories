package edu.csulb.memoriesapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private com.google.firebase.database.FirebaseDatabase firebaseDatabase;
    private final String TAG = "LoginActivity";
    private GoogleApiClient googleApiClient;
    private EditText userEmail;
    private EditText userPassword;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize all of the text and buttons
        //------------------------------------------------------------------------------------------------------------
        SignInButton signInButtonGoogle = (SignInButton) findViewById(R.id.sign_in_button_google);
        signInButtonGoogle.setSize(SignInButton.SIZE_STANDARD);
        signInButtonGoogle.setOnClickListener(this);
        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
        Button createAccountButton = (Button) this.findViewById(R.id.create_account_button);
        createAccountButton.setOnClickListener(this);
        userEmail = (EditText) findViewById(R.id.user_email_login);
        userPassword = (EditText) findViewById(R.id.user_password_login);
        //------------------------------------------------------------------------------------------------------------

        //Get Firebase instances and initialize GoogleSignInOptions and google's api client
        //-----------------------------------------------------------------------------------------------------------
        firebaseDatabase = com.google.firebase.database.FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        //-----------------------------------------------------------------------------------------------------------


        //State Listener, triggers everytime the user is signed in or signs out
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fUser = firebaseAuth.getCurrentUser();
                if (fUser != null) {
                    Log.d(TAG, "onAuthStateChanged: signed_in : " + fUser.getUid());
                    //User has successfully signed in, move to trending activity
                    Intent intent = new Intent(LoginActivity.this, TrendingActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser currUser = mAuth.getCurrentUser();
        if (currUser != null) {
            Intent intent = new Intent(this, TrendingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //Calls the appropriate methods depending on how the user wants to sign in
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button_google: {
                signInGoogle();
            }
            break;
            case R.id.sign_in_button: {
                signIn();
            }
            break;
            case R.id.create_account_button: {
                Intent createAccountIntent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(createAccountIntent);
            }
            break;
        }
    }

    //Attemps to retrieve info through google sign in
    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //Retrieved info from google sign in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                printToast("Unable to log in with google account.");
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                final FirebaseUser fUser = mAuth.getCurrentUser();
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");

                    //Access the database and see if the user exists
                    final DatabaseReference databaseReference = firebaseDatabase.getReference(UserDatabase.USERS);
                    databaseReference.child(fUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user == null) {
                                //If User does not exist in our database, create an instance of them in the database
                                databaseReference.child(fUser.getUid()).setValue(new User(fUser.getEmail(), fUser.getDisplayName()));
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    printToast("Authentication Failed.");
                }
            }
        });
    }


    //Connection has failed, Log that the connection has failed from this activity
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    //User has signed in with their email and password
    private void signIn() {
        String strUserEmail = userEmail.getText().toString();
        String strUserPassword = userPassword.getText().toString();

        if (strUserEmail.isEmpty()) {
            printToast("User Email field is empty");
        } else if (strUserPassword.isEmpty()) {
            printToast("User Password field is empty");
        } else {
            mAuth.signInWithEmailAndPassword(strUserEmail, strUserPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                            } else {
                                printToast("Account and password combination not found");
                            }
                        }
                    });
        }
    }

    private void printToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
