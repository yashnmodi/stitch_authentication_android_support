package project.airved;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;

public class RegisterUser extends AppCompatActivity {

    private EditText nameView;
    private EditText emailView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private Button registerBtn;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameView = (EditText) findViewById(R.id.name);
        emailView = (EditText) findViewById(R.id.newuser);
        passwordView = (EditText) findViewById(R.id.newpassword);
        confirmPasswordView = (EditText) findViewById(R.id.conpassword);
        registerBtn = (Button) findViewById(R.id.registerbtn);
        loginLink = (TextView) findViewById(R.id.loginlink);

        emailView.setError(null);
        passwordView.setError(null);
        confirmPasswordView.setError(null);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRegister();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterUser.this, LoginActivity.class));
            }
        });
    }

    private void getRegister() {
        final String name = nameView.getText().toString();
        final String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        String conpassword = confirmPasswordView.getText().toString();
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

        if (!password.equals(conpassword)) {
            confirmPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = confirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            UserPasswordAuthProviderClient emailPassClient = LoginActivity.client.getAuth().getProviderClient(UserPasswordAuthProviderClient.factory);
            emailPassClient.registerWithEmail(email, password).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull final Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("stitch", "Successfully sent account confirmation email");
                        Toast.makeText(RegisterUser.this, "Mail sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("stitch", "Error registering new user:", task.getException());
                        Toast.makeText(RegisterUser.this, "Error!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}


