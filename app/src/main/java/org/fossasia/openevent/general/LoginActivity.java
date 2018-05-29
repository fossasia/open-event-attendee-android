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

    private ProgressDialog progressDialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = SharedPreferencesUtil.getString(ConstantStrings.TOKEN,null);
        Timber.d("Token is %s", token);
        if (token != null)
            redirectToMain();

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Logging you in...");

        loginBtn.setOnClickListener(v -> {
            progressDialog.show();
            loginUser(usernameET.getText().toString(), passwordET.getText().toString());
        });
    }

    public void redirectToMain(){
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginUser(String email, String password) {
        Login login = new Login(email.trim(), password.trim()) ;
        compositeDisposable.add(ApiClient.getEventApi().login(login)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginResponse -> {
                    Toast.makeText(getApplicationContext(), "Success! " , Toast.LENGTH_LONG).show();
                    ApiClient.setToken(loginResponse.getAccessToken());
                    progressDialog.cancel();
                    SharedPreferencesUtil.putString(ConstantStrings.TOKEN, loginResponse.getAccessToken());
                    if(loginResponse.getAccessToken() != null)
                        redirectToMain();
                }, throwable -> {
                    ApiClient.setToken(null);
                    progressDialog.cancel();
                    Toast.makeText(getApplicationContext(), "Unable to Login!" , Toast.LENGTH_LONG).show();
                    Timber.e("Failure"+"\n"+throwable.toString());
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}