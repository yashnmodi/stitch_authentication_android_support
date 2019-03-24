package project.airved;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;

public class LoginActivity extends AppCompatActivity {


    private EditText emailView;
    private EditText passwordView;
    private Button signInBtn;
    private TextView registerLink;
    private TextView resetPassword;
    private StitchUser currentUser;

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        Splash.client = Stitch.getAppClient("YOUR APP-ID");
        currentUser = Splash.client.getAuth().getUser();
        if(currentUser != null){
            startActivity(new Intent(LoginActivity.this, Dashboard.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailView = (EditText) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        signInBtn = (Button) findViewById(R.id.signinbtn);
        registerLink = (TextView) findViewById(R.id.registerlink);
        resetPassword = (TextView) findViewById(R.id.resetpassword);
        emailView.setError(null);
        passwordView.setError(null);

        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        signInBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recoverPassword();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterUser.class));
            }
        });
    }

    private void attemptLogin() {
        final String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!MiscFunc.isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) && !MiscFunc.isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            //UserPasswordCredential credential = new UserPasswordCredential(params[0], params[1]);
            new getLoginTask().execute(email,password);
        }
    }

    private class getLoginTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog pd;
        private byte statusCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(LoginActivity.this, "", "Airis is recognizing you...", true, false); // Create and show Progress dialog
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                UserPasswordCredential credential = new UserPasswordCredential(params[0], params[1]);
                Splash.client.getAuth().loginWithCredential(credential).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()){
                            statusCode = 1;
                            onPostExecute();
                        }
                        else{
                            statusCode = 0;
                            onPostExecute();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("stitch-auth", "Authentication Failed!");
            }
            return null;
        }

        protected void onPostExecute() {
            if (statusCode == 1) {
                Log.d("stitch-auth", "Authentication Successful.");
                Toast.makeText(LoginActivity.this, "Airis welcomes you!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, Dashboard.class));
                pd.dismiss();
                finish();
            } else {
                Log.e("stitch-auth", "Authentication Failed.");
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "Ugh...! Error in recognizing.", Toast.LENGTH_SHORT).show();
                passwordView.setText("");
            }
        }
    }

    private void recoverPassword() {
        String email = emailView.getText().toString();
        UserPasswordAuthProviderClient emailPassClient = Stitch.getDefaultAppClient().getAuth().getProviderClient(UserPasswordAuthProviderClient.factory);
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
        } else {
            emailPassClient.sendResetPasswordEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull final Task<Void> task) {
                                                   if (task.isSuccessful()) {
                                                       Log.d("stitch-auth", "Successfully sent password reset email");
                                                       Toast.makeText(LoginActivity.this, "Password reset mail sent.", Toast.LENGTH_SHORT).show();
                                                   } else {
                                                       Log.e("stitch-auth", "Error sending password reset email:", task.getException());
                                                       Toast.makeText(LoginActivity.this, "Account not found!", Toast.LENGTH_SHORT).show();
                                                   }
                                               }
                                           }
                    );
        }
    }
}
