package org.republica.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.republica.R;
import org.republica.model.Sponsor;
import org.republica.utils.VolleySingleton;

import java.util.ArrayList;

/**
 * Created by manan on 25-03-2015.
 */
public class SponsorAdapter extends BaseAdapter {
    private ArrayList<Sponsor> mSponsorList;
    private Context mContext;
    private LayoutInflater mInflater;

    public SponsorAdapter(Context context, ArrayList<Sponsor> sponsorList) {
        this.mSponsorList = sponsorList;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mSponsorList.size();
    }

    @Override
    public Sponsor getItem(int position) {
        return mSponsorList.get(position);
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
            row = mInflater.inflate(R.layout.item_sponsor, parent, false);
        } else {
            row = convertView;
        }

        SponsorHolder holder = new SponsorHolder();
        holder.sponsorImage = (NetworkImageView) row.findViewById(R.id.imageView_sponsor);
        holder.sponsorImage.setDefaultImageResId(R.drawable.fossasia_sponsor);
        final Sponsor sponsor = getItem(position);
        String url = sponsor.getImg();
        holder.sponsorImage.setImageUrl(url, VolleySingleton.getImageLoader(mContext));
       row.setTag(sponsor);
        return row;
    }

    public static class SponsorHolder {
        TextView name;
        NetworkImageView sponsorImage;

    }
}



