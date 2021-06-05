package com.example.rider_atrafficsolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity implements View.OnClickListener {


    EditText phoneNumberOTP;
    Button resendOTPButton,verifyOTPButton;
    String name,phone_number,email,password;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private AlertDialog.Builder builder;

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        this.setTitle("Phone Number OTP Verifier");
        phoneNumberOTP=findViewById(R.id.phoneNumberOTP);
        resendOTPButton=findViewById(R.id.resendOTPButton);
        verifyOTPButton=findViewById(R.id.otpVerifyButton);
        phoneNumberOTP=findViewById(R.id.phoneNumberOTP);
        Intent intent=getIntent();
        phone_number=intent.getStringExtra("PhoneNumber");
        email=intent.getStringExtra("email");
        name=intent.getStringExtra("name");
        password =intent.getStringExtra("password");
        phoneNumberOTP.setHint("Enter OTP sent to "+phone_number);
        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference("users");
        //SaveUserData();

        verifyOTPButton.setOnClickListener(this);
        resendOTPButton.setOnClickListener(this);
        resendOTPButton.setEnabled(false);

        //alert

        builder = new AlertDialog.Builder(this);

        builder.setMessage("Click Ok to Continue")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                       // mAuth.getCurrentUser().sendEmailVerification();

                        mAuth.getCurrentUser().linkWithCredential(credential)
                                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            SaveUserData();
                                            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                                            startActivity(intent);

                                        } else {
                                            if(task.getException() instanceof FirebaseAuthUserCollisionException)
                                            {
                                                Toast.makeText(getApplicationContext(),"Already Registered",Toast.LENGTH_SHORT).show();
                                                Intent intent=new Intent(getApplicationContext(),SignUpActivity.class);
                                                startActivity(intent);
                                            }
                                            else
                                            {
                                                Toast.makeText(getApplicationContext(),"Error"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }
                                });
                    }
                });

        initiateOTP();

    }

    private void SaveUserData()
    {
        //User user=null;
      //  if(MainActivity.isuserTypeSwitchChecked)
        mAuth.getCurrentUser().sendEmailVerification();
//       // String key=databaseReference.push().getKey();
//        String key=mAuth.getCurrentUser().getUid();
//        Passenger user=new Passenger(name, email, phone_number);
//        databaseReference.child(key).setValue(user);
//        Log.d("database",user.toString());

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "https://rider-a-traffic-solution-default-rtdb.firebaseio.com/users.json";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("phone_number", phone_number);

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError
                {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void initiateOTP() {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone_number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(PhoneAuthActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(getApplicationContext(),"Verification Failed",Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationid, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                               super.onCodeSent(verificationid, forceResendingToken);
                                mVerificationID =verificationid;
                                mResendToken=forceResendingToken;
                                resendOTPButton.setVisibility(View.VISIBLE);

                                countDownTimer=new CountDownTimer(60000,1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        resendOTPButton.setText("RESEND OTP In "+millisUntilFinished/1000 +" seconds");
                                    }

                                    @Override
                                    public void onFinish() {

                                        resendOTPButton.setEnabled(true);
                                        resendOTPButton.setText("RESEND OTP NOW ");
                                    }
                                }.start();

                            }

                            @Override
                            public void onCodeAutoRetrievalTimeOut(@NonNull String verificationID) {
                                super.onCodeAutoRetrievalTimeOut(verificationID);


                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Verified","Verified");
                        phoneNumberOTP.setVisibility(View.GONE);
                        resendOTPButton.setVisibility(View.GONE);
                        verifyOTPButton.setVisibility(View.GONE);
                    //    SaveUserData();

                        AlertDialog alert = builder.create();
                        //Setting the title manually
                        alert.setTitle("PHONE NUMBER VERIFIED");
                        alert.show();



                    } else {

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            Toast.makeText(getApplicationContext(),"Verification Failed+"+task.getException().getMessage(),Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.otpVerifyButton:
              //  Log.d("Bal","Bal");
                PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(mVerificationID,phoneNumberOTP.getText().toString().trim());
                signInWithPhoneAuthCredential(phoneAuthCredential);
                break;
            case R.id.resendOTPButton:
                resendVerificationCode(phone_number,mResendToken);
                break;
        }
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(getApplicationContext(),"Verification Failed",Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);
                                mVerificationID =verificationId;
                                mResendToken=forceResendingToken;
                                countDownTimer=new CountDownTimer(60000,1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        resendOTPButton.setText("RESEND OTP In "+millisUntilFinished/1000 +" seconds");
                                    }

                                    @Override
                                    public void onFinish() {

                                        resendOTPButton.setEnabled(true);
                                        resendOTPButton.setText("RESEND OTP NOW ");
                                    }
                                }.start();

                            }
                        })          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}