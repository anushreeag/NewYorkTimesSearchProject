package com.cosmic.newyorktimessearch.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cosmic.newyorktimessearch.R;
import com.cosmic.newyorktimessearch.activity.NYTSearchActivity;
import com.cosmic.newyorktimessearch.model.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anushree on 9/19/2017.
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    Context mCtx;
    ArrayList<Article> mList;

    public ArticleAdapter(Context ctx,ArrayList<Article> list){
        mCtx = ctx;
        mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(mCtx);
        View view = li.inflate(R.layout.item_recycleview,parent,false);
        ViewHolder holder = new ViewHolder(view,mCtx);
       // Log.i(NYTSearchActivity.TAG,"onCreateViewHolder");
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article article = mList.get(position);
        holder.bind(article);

       // Log.i(NYTSearchActivity.TAG,"onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        Context ctx;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            ctx = context;

            image = (ImageView )itemView.findViewById(R.id.thumbnail);
            title = (TextView )itemView.findViewById(R.id.title);
        }
        void bind(Article article){
            image.setImageResource(0);
            title.setText(article.getTitle());
            if(!article.getThumbnail().equals(""))
                Picasso.with(ctx).load(article.getThumbnail()).error(R.mipmap.ic_launcher).fit().into(image);
        }
    }

}
