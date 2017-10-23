package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Danie on 10/16/2017.
 */

public class CreateAccountActivity extends Activity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private EditText userEmail;
    private EditText userPassword;
    private EditText userPassword2;
    private Button createAccountButton;
    private final String TAG = "CreateAccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        mAuth = FirebaseAuth.getInstance();
        userEmail = (EditText) this.findViewById(R.id.user_email_create);
        userPassword = (EditText) this.findViewById(R.id.user_password_create);
        userPassword2 = (EditText) this.findViewById(R.id.user_password_create2);
        createAccountButton = (Button) this.findViewById(R.id.create_account_button_2);
        createAccountButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String userEmailCreate = userEmail.getText().toString();
        String userPass = userPassword.getText().toString();
        String userPass2 = userPassword2.getText().toString();

        if(userEmailCreate.isEmpty()) {
            printToast("Email line is empty.");
        }
        else if(userPass.isEmpty() || userPass2.isEmpty()) {
            printToast("Password line is empty.");
        }
        else if (!passwordGuidelineCheck(userPass)) {
            printToast("Passwords does not meet guidelines.");
        }
        else if(!userPass.equals(userPass2)) {
            printToast("Passwords do not match.");
        }

        mAuth.createUserWithEmailAndPassword(userEmailCreate, userPass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    //TODO: need to send email or phone verification
                } else {
                    try{
                        throw task.getException();
                    }catch(FirebaseAuthInvalidCredentialsException malformedEmailException) {
                        printToast("Email format is not valid.");
                    }catch(FirebaseAuthUserCollisionException duplicateEmail) {
                        printToast("Email already exists.");
                    }catch(Exception exception) {
                        Log.w(TAG, "Failure to create use email", task.getException());
                    }
                }
            }
        });
    }

    private boolean passwordGuidelineCheck(String password) {
        if(password.length() <= 6) {
            printToast("Password length must be longer than 6");
        }
        //TODO: figure out a algorithm to quickly check password strength
        return true;
    }

    private void printToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
