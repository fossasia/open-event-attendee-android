package org.fossasia.openevent.general;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.fossasia.openevent.general.rest.ApiClient;
import org.fossasia.openevent.general.utils.ConstantStrings;
import org.fossasia.openevent.general.utils.JWTUtils;
import org.fossasia.openevent.general.utils.SharedPreferencesUtil;
import org.json.JSONException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class ProfileFragment extends Fragment {

    private static final String app="application/vnd.api+json";
    private long userId=-1;
    private TextView firstNameTv;
    private TextView emailTv;
    private ImageView avatarImageView;
    private ProgressBar progressBar;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ProfileFragment(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String token = SharedPreferencesUtil.getString(ConstantStrings.TOKEN, null);
        if(token == null)
            redirectToLogin();
        token = "JWT "+ token;
        try {
            userId = JWTUtils.getIdentity(token);
            Timber.d("User id is %s", userId);
            ApiClient.setToken(token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void redirectToLogin() {
        Intent i =new Intent(getActivity(),LoginActivity.class);
        startActivity(i);
    }
    private void redirectToMain() {
        Intent i = new Intent(getActivity(),MainActivity.class);
        startActivity(i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firstNameTv = view.findViewById(R.id.first_name_tv);
        emailTv = view.findViewById(R.id.email_tv);
        CardView logout = view.findViewById(R.id.logout_btn);
        logout.setOnClickListener(v -> {
            SharedPreferencesUtil.remove(ConstantStrings.TOKEN);
            ApiClient.setToken(null);
            redirectToMain();
        });
        avatarImageView = view.findViewById(R.id.avatar_image_view);
        progressBar= view.findViewById(R.id.progressHeaderUser);

        progressBar.setIndeterminate(true);

        compositeDisposable.add(ApiClient.getEventApi().getProfile(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.GONE);
                    Timber.d("Response Success");
                    firstNameTv.setText(user.getFirstName());
                    emailTv.setText(user.getEmail());

                    Picasso.with(view.getContext())
                            .load(user.getAvatarUrl())
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .transform(new CircleTransform())
                            .into(avatarImageView);
                }, throwable -> {
                    Timber.e(throwable, "Failure");
                }));
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}