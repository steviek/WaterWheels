package com.sixbynine.waterwheels.offerdisplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.util.OfferUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public final class FeedAdapter extends ArrayAdapter<Offer> {

    private static final String[] IMAGES = {
            "https://pixabay.com/static/uploads/photo/2015/12/24/00/37/great-horned-owl-1106379_960_720.jpg",
            "https://pixabay.com/static/uploads/photo/2012/06/19/15/06/goat-50290_960_720.jpg",
            "https://pixabay.com/static/uploads/photo/2015/02/25/22/16/goose-649585_960_720.jpg",
            "https://pixabay.com/static/uploads/photo/2015/06/03/13/13/cats-796437_960_720.jpg",
            "https://pixabay.com/static/uploads/photo/2015/12/23/10/20/husky-1105338_960_720.jpg",
            "https://pixabay.com/static/uploads/photo/2015/03/26/09/43/animal-690159_960_720.jpg"
    };

    public FeedAdapter(Context context, List<Offer> offers) {
        super(context, R.layout.row_offer, offers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_offer, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        Offer offer = getItem(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.origin.setText(offer.getOrigin().getName());
        viewHolder.destination.setText(offer.getDestination().getName());
        viewHolder.timestamp.setText(OfferUtils.makePrettyTimestamp(getContext(), offer.getTime()));
        String url = FacebookManager.getProfilePicUrlFromId(offer.getPost().getFrom().getId());
        Picasso.with(parent.getContext())
                .load(url)
                .noFade()
                .into(viewHolder.profile);

        // Load image from 5 positions ahead to avoid jittery photo loading as scrolling down
        if (position < getCount() - 5) {
            Offer offer5 = getItem(position + 5);
            String url5 = FacebookManager.getProfilePicUrlFromId(offer5.getPost().getFrom().getId());
            Picasso.with(parent.getContext())
                    .load(url5)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                        }

                        @Override
                        public void onBitmapFailed(Drawable drawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable drawable) {}
                    });
        }

        if (offer.getPrice().isPresent()) {
            viewHolder.price.setVisibility(View.VISIBLE);
            viewHolder.price.setText("$" + offer.getPrice().get()); //let's not muck about with decimal points
        } else {
            viewHolder.price.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    private static final class ViewHolder {
        final TextView origin;
        final TextView destination;
        final TextView timestamp;
        final TextView price;
        final ImageView profile;

        ViewHolder(View v) {
            this.origin = (TextView) v.findViewById(R.id.origin);
            this.destination = (TextView) v.findViewById(R.id.destination);
            this.timestamp = (TextView) v.findViewById(R.id.timestamp);
            this.price = (TextView) v.findViewById(R.id.price);
            this.profile = (ImageView) v.findViewById(R.id.profile_image);
        }
    }
}
