package org.fossasia.openevent.general;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.fossasia.openevent.general.model.Login;
import org.fossasia.openevent.general.rest.ApiClient;
import org.fossasia.openevent.general.utils.ConstantStrings;
import org.fossasia.openevent.general.utils.SharedPreferencesUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.username_et)
    protected EditText usernameET;
    @BindView(R.id.password_et)
    protected EditText passwordET;
    @BindView(R.id.login_btn)
    protected Button loginBtn;

    ProgressDialog progressDialog;
    public static String TOKEN=null;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = SharedPreferencesUtil.getString(ConstantStrings.TOKEN,null);
        Timber.d("Token is "+token);
        if(token != null)
            redirectToMain();

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        Intent i = getIntent();
        String temp = i.getStringExtra("LOGOUT");
        if(temp != null && temp.equals("TRUE")){
            TOKEN = null;
        }

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Logging you in...");

        loginBtn.setOnClickListener(v -> {
            //loginUser(ei.getText().toString(),e2.getText().toString());
            loginUser("hey@hey.hey", "heyheyhey");
            progressDialog.show();
        });
    }

    public void redirectToMain(){
        Intent i = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(i);
        this.finish();
    }

    public void loginUser(String title, String body) {

        Login login=new Login(title.trim(),body.trim()) ;
        compositeDisposable.add(ApiClient.getClient2(TOKEN).login(login)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if(response.isSuccessful()) {
                        String accessToken = response.body().getAccessToken();
                        Toast.makeText(getApplicationContext(), "Success! " , Toast.LENGTH_LONG).show();
                        TOKEN = accessToken;
                    } else {
                        Toast.makeText(getApplicationContext(), "Error Occured "+response.code(), Toast.LENGTH_LONG).show();
                        Timber.d("Error "+response.code()+"\n"+"Error body "+response.errorBody());
                    }
                    progressDialog.cancel();
                    SharedPreferencesUtil.putString(ConstantStrings.TOKEN,TOKEN);
                    if(TOKEN!=null)
                        redirectToMain();
                }, throwable -> {
                    TOKEN = null;
                    progressDialog.cancel();
                    Toast.makeText(getApplicationContext(), "Unable to Login !" , Toast.LENGTH_LONG).show();
                    Timber.e("Failure"+"\n"+throwable.toString());
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}