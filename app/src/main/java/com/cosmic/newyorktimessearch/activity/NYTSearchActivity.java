package com.cosmic.newyorktimessearch.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.cosmic.newyorktimessearch.R;
import com.cosmic.newyorktimessearch.adapter.ArticleAdapter;
import com.cosmic.newyorktimessearch.fragment.SearchFilterFragment;
import com.cosmic.newyorktimessearch.model.Article;
import com.cosmic.newyorktimessearch.utils.EndlessRecyclerViewScrollListener;
import com.cosmic.newyorktimessearch.utils.ItemClickSupport;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class NYTSearchActivity extends AppCompatActivity {

    public static final String API_KEY = "72b7f2dfb0764545a2e8378566e2d0af";
    public static final String NYTSEARCH_URL = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    public static final String TAG = "NYTSearchActivity";

    public static final String PREFERENCES = "nytsearch_pref";
    public static final String ARTS = "arts";
    public static final String SPORTS = "sports";
    public static final String FASHION = "fashion";
    public static final String BEGIN_DATE = "begin_dt";
    public static final String SORT_ORDER = "sort_order";
    public static final String WEB_URL = "web_url";
    EndlessRecyclerViewScrollListener scrollListener;
    SharedPreferences pref;
    ArrayList<Article> articleList;
    ProgressDialog progress;
    EditText query;
    Button searchbtn;
    RecyclerView nyGrid;
    ArticleAdapter adp ;
    AsyncHttpClient client;
    SearchFilterFragment filterFrag;
    FragmentManager fm;
    StaggeredGridLayoutManager gridLayoutManager;
    RequestParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nytsearch);


        pref = getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        client = new AsyncHttpClient();
        progress = new ProgressDialog(this);
        query = (EditText)findViewById(R.id.query);
        searchbtn = (Button) findViewById(R.id.search_btn);
        nyGrid = (RecyclerView) findViewById(R.id.nyView);
        articleList = new ArrayList<>();
        adp = new ArticleAdapter(this,articleList);
        if(!isOnline()) {
            Toast.makeText(NYTSearchActivity.this, "Internet is not available, Please Connect to Wifi or Data ", Toast.LENGTH_LONG).show();
            searchbtn.setEnabled(false);
        }
        fm = getSupportFragmentManager();
        ItemClickSupport.addTo(nyGrid).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            Article article = articleList.get(position);
                            Toast.makeText(NYTSearchActivity.this,"Loading the webpage. Please wait..",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.setClass(NYTSearchActivity.this,WebViewActivity.class);
                            intent.putExtra(WEB_URL,article.getWeb_url());
                            startActivity(intent);
                    }
                }
        );
        gridLayoutManager = new StaggeredGridLayoutManager(2,1);
        nyGrid.setLayoutManager(gridLayoutManager);
        nyGrid.setAdapter(adp);
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG,"Total Items ="+totalItemsCount);
                Log.i(TAG,"page ="+page);
                loadNextDataFromApi(page);
            }
        };
        nyGrid.addOnScrollListener(scrollListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isOnline()) {
            searchbtn.setEnabled(true);
        }
    }

    public void loadNextDataFromApi(int offset) {
        params.remove("page");
        params.add("page",""+offset);

        Log.i(TAG,"loadNextDataFromApi params = "+params.toString());
        client.get(NYTSEARCH_URL,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray result = (JSONArray) response.getJSONObject("response").getJSONArray("docs");
                    int cursize = articleList.size();
                    ArrayList<Article> moreArticles = Article.getArticleList(result);
                    articleList.addAll(moreArticles);
                    adp.notifyItemRangeInserted(cursize, articleList.size()-1);
                    Log.i(TAG, articleList.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i(TAG, "Search Failed");
                Log.i(TAG,""+errorResponse.toString());
                final String error = errorResponse.toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NYTSearchActivity.this,error,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "Search Failed");
                Log.i(TAG,""+responseString);
                final String error = responseString;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NYTSearchActivity.this,error,Toast.LENGTH_SHORT).show();
                    }
                });

            }


        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater in = getMenuInflater();
        in.inflate(R.menu.article_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        filterFrag = new SearchFilterFragment();
        filterFrag.show(fm,"filter");
        return true;
    }

    public void searchArticle(View view) {
        String query_text = query.getText().toString();
        params = new RequestParams();
        if (!query_text.isEmpty()) {
            searchbtn.setEnabled(false);
            progress.setMessage("Searching in progress.Please wait...");
            progress.show();
            params.add("apikey", API_KEY);
            params.add("q", query_text);
            int curSize = articleList.size();
            articleList.clear();
            adp.notifyItemRangeRemoved(0, curSize);
            scrollListener.resetState();
            String date = pref.getString(BEGIN_DATE, "");
            if (!(date.isEmpty())) {
                StringBuilder datestr = new StringBuilder();
                datestr.append(date.split("/")[2]).append(date.split("/")[0]).append(date.split("/")[1]);
                params.add("begin_date", datestr.toString());
            }
            int sort_order = pref.getInt(SORT_ORDER, 0);
            if (sort_order == 1)
                params.add("sort", "newest");
            else
                params.add("sort", "oldest");

            Boolean arts = pref.getBoolean(ARTS, false);
            Boolean sports = pref.getBoolean(SPORTS, false);
            Boolean fashion = pref.getBoolean(FASHION, false);
            //params.add("page", "1");
            if (arts || sports || fashion) {
                params.add("fq", getnews_desk(arts, sports, fashion));
            }
            Log.i(TAG, params.toString());

                client.get(NYTSEARCH_URL, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i(TAG, "Search Success");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                searchbtn.setEnabled(true);
                                Toast.makeText(NYTSearchActivity.this, "Search Success", Toast.LENGTH_SHORT).show();
                            }
                        });
                        try {
                            JSONArray result = (JSONArray) response.getJSONObject("response").getJSONArray("docs");
                            articleList.addAll(Article.getArticleList(result));
                            adp.notifyItemRangeInserted(0, articleList.size());
                            Log.i(TAG, articleList.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.i(TAG, "Search Failed");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                searchbtn.setEnabled(true);
                                Toast.makeText(NYTSearchActivity.this, "Failed to Search", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.i(TAG, "Search Failed");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                searchbtn.setEnabled(true);
                                Toast.makeText(NYTSearchActivity.this, "Failed to Search", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }



                });


        }
    }

    String getnews_desk(Boolean arts, Boolean sports, Boolean fashion){
        StringBuilder new_desk = new StringBuilder();
        new_desk.append("news_desk:(");
        if(arts)
            new_desk.append("\"Arts\"");
        if(sports) {
            if (new_desk.length() >=17)
                new_desk.append(" \"Sports\"");
            else
                new_desk.append("\"Sports\"");
        }
        if(fashion){
            if (new_desk.length() >=17)
                new_desk.append(" \"Fashion Style\"");
            else
                new_desk.append("\"Fashion Style\"");
        }

        new_desk.append(")");

        Log.i(TAG,"new_desk = "+new_desk.toString());
        return new_desk.toString();


    }


    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
