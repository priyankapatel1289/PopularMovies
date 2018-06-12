package patel.priyanka.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder> {
    private List<ReviewsModel> reviewsList;

    public ReviewsAdapter(List<ReviewsModel> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @Override
    public ReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reviews_layout, parent, false);
        return new ReviewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewsViewHolder holder, int position) {
        ReviewsModel reviewsModel = reviewsList.get(position);

        holder.reviewAuthor.setText(reviewsModel.getAuthor());
        holder.reviewContent.setText(reviewsModel.getContent());

    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    class ReviewsViewHolder extends RecyclerView.ViewHolder {
        final TextView reviewAuthor;
        final TextView reviewContent;

        ReviewsViewHolder(View itemView) {
            super(itemView);
            reviewAuthor = (TextView) itemView.findViewById(R.id.tv_reviews_author);
            reviewContent = (TextView) itemView.findViewById(R.id.tv_reviews_content);
        }
    }
}

