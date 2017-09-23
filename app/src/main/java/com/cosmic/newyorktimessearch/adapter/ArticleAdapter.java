package com.cosmic.newyorktimessearch.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.cosmic.newyorktimessearch.R;
import com.cosmic.newyorktimessearch.databinding.ItemRecycleviewBinding;
import com.cosmic.newyorktimessearch.databinding.ItemRecycleviewImageBinding;
import com.cosmic.newyorktimessearch.model.Article;
import java.util.ArrayList;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by anushree on 9/19/2017.
 */

public class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context mCtx;
    ArrayList<Article> mList;

    final int HAS_IMAGE = 1, NO_IMAGE=0;

    public ArticleAdapter(Context ctx,ArrayList<Article> list){
        mCtx = ctx;
        mList = list;
    }

    @Override
    public int getItemViewType(int position) {
        if(mList.get(position).getThumbnail().isEmpty())
            return NO_IMAGE;

        return HAS_IMAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(mCtx);
        RecyclerView.ViewHolder viewHolder = null;
       switch (viewType){
           case NO_IMAGE:
               View view1 = li.inflate(R.layout.item_recycleview,parent,false);
               viewHolder = new ViewHolder1(view1,mCtx);
               return viewHolder;
           case HAS_IMAGE:
               View view2 = li.inflate(R.layout.item_recycleview_image,parent,false);
               viewHolder = new ViewHolder2(view2,mCtx);
               return viewHolder;
           default:
               try {
                   throw new Exception("Unknown type in the list");
               } catch (Exception e) {
                   e.printStackTrace();
               }
       }

       return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Article article = mList.get(position);
        switch(holder.getItemViewType()){
            case NO_IMAGE:
                ViewHolder1 vh1 = (ViewHolder1) holder;
                vh1.bind(article);
                break;
            case HAS_IMAGE:
                ViewHolder2 vh2 = (ViewHolder2) holder;
                vh2.bind(article);
                break;
            default:
                try {
                    throw new Exception("Unknown type in the list");
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }

       // Log.i(NYTSearchActivity.TAG,"onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder1 extends RecyclerView.ViewHolder {

        ItemRecycleviewBinding binding;
        TextView title;
        TextView label;
        TextView desc;
        Context ctx;

        public ViewHolder1(View itemView, Context context) {
            super(itemView);
            binding =  ItemRecycleviewBinding.bind(itemView);
            title = binding.title;
            label = binding.label;
            desc = binding.desc;
            ctx = context;
        }

        void bind(Article article) {

            title.setText(article.getTitle());
            label.setText(article.getNew_desk().toUpperCase());
            desc.setText(article.getSnippet());

            if(article.getNew_desk().toUpperCase().contains("ARTS")) label.setBackgroundColor(ctx.getResources().getColor(R.color.blue));
            else if(article.getNew_desk().toUpperCase().contains("SPORTS")) label.setBackgroundColor(ctx.getResources().getColor(R.color.orange));
            else if(article.getNew_desk().toUpperCase().contains("FASHION & STYLE")) label.setBackgroundColor(ctx.getResources().getColor(R.color.brown));
            else label.setBackgroundColor(ctx.getResources().getColor(R.color.red));
        }
    }

    public static class ViewHolder2 extends RecyclerView.ViewHolder {

        ItemRecycleviewImageBinding binding;
        ImageView image;
        TextView title;
        Context ctx;
        TextView label;
        TextView desc;

        public ViewHolder2(View itemView, Context context) {
            super(itemView);
            ctx = context;

            binding =  ItemRecycleviewImageBinding.bind(itemView);
            title = binding.title;
            label = binding.label;
            desc = binding.desc;
            image =  binding.thumbnail;
        }
        void bind(Article article){
            image.setImageResource(0);
            title.setText(article.getTitle());
            label.setText(article.getNew_desk().toUpperCase());
            if(article.getNew_desk().toUpperCase().contains("ARTS")) label.setBackgroundColor(ctx.getResources().getColor(R.color.blue));
            else if(article.getNew_desk().toUpperCase().contains("SPORTS")) label.setBackgroundColor(ctx.getResources().getColor(R.color.orange));
            else if(article.getNew_desk().toUpperCase().contains("FASHION & STYLE")) label.setBackgroundColor(ctx.getResources().getColor(R.color.brown));
            else label.setBackgroundColor(ctx.getResources().getColor(R.color.red));

            desc.setText(article.getSnippet());
            if(!article.getThumbnail().equals(""))
                Glide.with(ctx).load(article.getThumbnail()).placeholder(R.drawable.place_holder).error(R.drawable.place_holder_error).into(image);
        }
    }

}
