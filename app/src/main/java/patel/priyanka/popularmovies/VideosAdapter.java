package patel.priyanka.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosViewHolder> {

    private static final String TAG = VideosAdapter.class.getSimpleName();
    private List<VideosModel> videosList = new ArrayList<>();
    Context mContext;

    public VideosAdapter(List<VideosModel> videosList) {
        this.videosList = videosList;
    }

    @Override
    public VideosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.videos_layout, parent, false);
        return new VideosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideosViewHolder holder, int position) {
        final VideosModel videosModel = videosList.get(position);
        holder.imagePlayIcon.setImageResource(R.drawable.icn_play);
        final Context context = holder.imageYoutubeThumbnail.getContext();
        holder.imageYoutubeThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videosModel.getVideoKey()));
                context.startActivity(intent);
            }
        });

        try {
            String videoId=extractYoutubeId("http://www.youtube.com/watch?v=" + videosModel.getVideoKey());

            Log.e(TAG,"VideoId is = " + videoId);
            String imageURL = "http://img.youtube.com/vi/"+videoId+"/0.jpg";
            Picasso.with(mContext).load(imageURL).error(android.R.drawable.stat_sys_warning).into(holder.imageYoutubeThumbnail);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    public String extractYoutubeId(String url) throws MalformedURLException {
        String query = new URL(url).getQuery();
        String[] param = query.split("&");
        String id = null;
        for (String row : param) {
            String[] param1 = row.split("=");
            if (param1[0].equals("v")) {
                id = param1[1];
            }
        }
        return id;
    }

    public class VideosViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageYoutubeThumbnail;
        private ImageView imagePlayIcon;

        public VideosViewHolder(View itemView) {
            super(itemView);
            imageYoutubeThumbnail = (ImageView) itemView.findViewById(R.id.image_thumbnail);
            imagePlayIcon = (ImageView) itemView.findViewById(R.id.image_play_pause);
        }

    }
}

