package org.republica.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import org.republica.R;
import org.republica.model.Speaker;
import org.republica.utils.VolleySingleton;

import java.util.ArrayList;

/**
 * Created by Abhishek on 14/02/15.
 */
public class SpeakerAdapter extends BaseAdapter {

    private ArrayList<Speaker> mSpeakerList;
    private Context mContext;
    private LayoutInflater mInflater;

    public SpeakerAdapter(Context context, ArrayList<Speaker> speakerList) {
        this.mSpeakerList = speakerList;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mSpeakerList.size();
    }

    @Override
    public Speaker getItem(int position) {
        return mSpeakerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (mInflater == null) {
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        View row;
        if (convertView == null) {
            row = mInflater.inflate(R.layout.item_speaker, parent, false);
        } else {
            row = convertView;
        }

        SpeakerHolder holder = new SpeakerHolder();
        holder.name = (TextView) row.findViewById(R.id.textView_speaker_name);
        holder.designation = (TextView) row.findViewById(R.id.textView_speaker_designation);
        holder.speakerImage = (NetworkImageView) row.findViewById(R.id.imageView_speaker_pic);
        holder.speakerImage.setDefaultImageResId(R.drawable.default_user);
        holder.information = (TextView) row.findViewById(R.id.textView_speaker_information);
        holder.linkedIn = (ImageView) row.findViewById(R.id.imageView_linkedin);
        holder.twitter = (ImageView) row.findViewById(R.id.imageView_twitter);
        final Speaker speaker = getItem(position);
        if (speaker.getProfilePicUrl() != null && speaker.getProfilePicUrl().equals("")) {
            holder.speakerImage.setImageUrl("http://forschdb.verwaltung.uni-freiburg.de/pix/forschdb/mitarbeiter_12821_20141208121645.png", VolleySingleton.getImageLoader(mContext));
        } else {
            holder.speakerImage.setImageUrl(speaker.getProfilePicUrl(), VolleySingleton.getImageLoader(mContext));
        }

        holder.name.setText(speaker.getName());
        holder.designation.setText(speaker.getDesignation());

        if (speaker.getInformation().equals("")) {
            holder.information.setVisibility(View.GONE);
        } else {
            holder.information.setVisibility(View.VISIBLE);
            holder.information.setText(speaker.getInformation());
        }
        if (speaker.getLinkedInUrl().length() == 0 || speaker.getLinkedInUrl().equals("")) {
            holder.linkedIn.setVisibility(View.GONE);
        } else {
            holder.linkedIn.setVisibility(View.VISIBLE);
            holder.linkedIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = speaker.getLinkedInUrl();
                    if(URLUtil.isValidUrl(url)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);

                    }
                    else {
                        Toast.makeText(mContext, "Invalid Linkedin handle", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (speaker.getTwitterHandle().length() == 0 || speaker.getTwitterHandle().equals("")) {
            holder.twitter.setVisibility(View.GONE);
        } else {
            holder.twitter.setVisibility(View.VISIBLE);
            holder.twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String url = speaker.getTwitterHandle();
                    if(URLUtil.isValidUrl(url)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);

                    }
                    else if(url.contains("@")) {
                        url = url.replace("@", "");
                        url = "http://twitter.com/" + url;
                        if(URLUtil.isValidUrl(url))
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }
                    else {
                        Toast.makeText(mContext, "Invalid twitter handle", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        row.setTag(speaker);
        return row;
    }

//    @Override
//    public boolean isEnabled(int position) {
//        return false;
//    }

    public static class SpeakerHolder {
        TextView name;
        TextView designation;
        NetworkImageView speakerImage;
        TextView information;
        ImageView linkedIn;
        ImageView twitter;

    }
}
