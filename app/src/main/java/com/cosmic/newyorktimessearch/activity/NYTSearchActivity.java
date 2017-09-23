package com.cosmic.newyorktimessearch.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cosmic.newyorktimessearch.R;
import com.cosmic.newyorktimessearch.adapter.ArticleAdapter;
import com.cosmic.newyorktimessearch.broadcastReceiver.NetworkStateReceiver;
import com.cosmic.newyorktimessearch.databinding.ActivityNytsearchBinding;
import com.cosmic.newyorktimessearch.fragment.SearchFilterFragment;
import com.cosmic.newyorktimessearch.model.Article;
import com.cosmic.newyorktimessearch.utils.EndlessRecyclerViewScrollListener;
import com.cosmic.newyorktimessearch.utils.ItemClickSupport;
import com.cosmic.newyorktimessearch.utils.MyDividerItemDecoration;
import com.cosmic.newyorktimessearch.utils.VerticalSpaceItemDecoration;
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
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class NYTSearchActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener,SearchFilterFragment.SaveFilterListener{

    private static final String API_KEY = "72b7f2dfb0764545a2e8378566e2d0af";
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

    ArrayList<Article> articleList;
    ProgressDialog progress;
    TextView empty;
    Button searchbtn;
    Bundle data;
    RecyclerView nyGrid;
    ArticleAdapter adp ;
    AsyncHttpClient client;
    SearchFilterFragment filterFrag;
    FragmentManager fm;
    StaggeredGridLayoutManager gridLayoutManager;
    RequestParams params;
    Toolbar toolbar;
    Context mCtx;
    private NetworkStateReceiver networkStateReceiver;
    private Menu menu;
    private ActivityNytsearchBinding binding;
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nytsearch);
        client = new AsyncHttpClient();
        progress = new ProgressDialog(this);
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        toolbar = binding.appBar.toolbar;
        mCtx = NYTSearchActivity.this;
        setSupportActionBar(toolbar);
        data = new Bundle();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView title  = (TextView) binding.appBar.toolbarTitle;
        title.setText("NewYorkTimes Search");
        nyGrid = binding.nyView;
        empty = binding.emptyView;
        articleList = new ArrayList<>();
        adp = new ArticleAdapter(this,articleList);
        fm = getSupportFragmentManager();
        ItemClickSupport.addTo(nyGrid).setOnItemClickListener(
                (recyclerView, position, v) -> {
                        Article article = articleList.get(position);
                        Toast.makeText(mCtx,"Loading the webpage. Please wait..",Toast.LENGTH_SHORT).show();
                        /*
                            WebView Implementation begins
                         */
                           /* Intent intent = new Intent();
                            intent.setClass(mCtx,WebViewActivity.class);
                            intent.putExtra(WEB_URL,article.getWeb_url());
                            startActivity(intent);*/
                        /*
                            WebView Implementation end
                         */
                       /*
                        *Chrome custom tab implementation begins
                        */
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.action_share);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, article.getWeb_url());
                        int requestCode = 100;

                        PendingIntent pendingIntent = PendingIntent.getActivity(mCtx,
                                requestCode,
                                shareIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(ContextCompat.getColor(mCtx, R.color.colorAccent));
                        builder.setActionButton(bitmap, "Share this Link", pendingIntent, true);
                        // set toolbar color and/or setting custom actions before invoking build()
                        // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
                        CustomTabsIntent customTabsIntent = builder.build();
                        // and launch the desired Url with CustomTabsIntent.launchUrl()
                        customTabsIntent.launchUrl(mCtx, Uri.parse(article.getWeb_url()));


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


        if(articleList.size()==0)
        {
            nyGrid.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }
        VerticalSpaceItemDecoration divider = new VerticalSpaceItemDecoration(5);
        nyGrid.addItemDecoration(divider);
       // nyGrid.addItemDecoration(new MyDividerItemDecoration(nyGrid.getContext(),R.drawable.mydivider));
        nyGrid.setItemAnimator(new SlideInUpAnimator());
        fillBundleFromPreferences();
    }

    private void fillBundleFromPreferences(){
        pref = getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        editor = pref.edit();
        data.putString(BEGIN_DATE,pref.getString(BEGIN_DATE,""));
        data.putInt(SORT_ORDER,pref.getInt(SORT_ORDER,0));
        data.putBoolean(ARTS,pref.getBoolean(ARTS,false));
        data.putBoolean(SPORTS,pref.getBoolean(SPORTS,false));
        data.putBoolean(FASHION,pref.getBoolean(FASHION,false));
    }

    private void fillPreferencesFromBundle(){
        editor.putString(BEGIN_DATE,data.getString(BEGIN_DATE,""));
        editor.putInt(SORT_ORDER,data.getInt(SORT_ORDER,0));
        editor.putBoolean(ARTS,data.getBoolean(ARTS,false));
        editor.putBoolean(SPORTS,data.getBoolean(SPORTS,false));
        editor.putBoolean(FASHION,data.getBoolean(FASHION,false));
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void onDestroy() {
        super.onDestroy();
        fillPreferencesFromBundle();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
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
                        Toast.makeText(mCtx,error,Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(mCtx,error,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater in = getMenuInflater();
        in.inflate(R.menu.article_menu,menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if(!isOnline()) {
            searchItem.setEnabled(false);
        }
        else{
            searchItem.setEnabled(true);
        }
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchArticle(query);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.filter) {
            filterFrag = new SearchFilterFragment();
            filterFrag.setArguments(data);
            filterFrag.show(fm, "filter");
        }
        return true;
    }

    public void searchArticle(String query) {
        params = new RequestParams();
        if (!query.isEmpty()) {
            progress.setMessage("Searching in progress.Please wait...");
            progress.show();
            params.add("apikey", API_KEY);
            params.add("q", query);
            int curSize = articleList.size();
            articleList.clear();
            adp.notifyItemRangeRemoved(0, curSize);
            scrollListener.resetState();
            String date = data.getString(BEGIN_DATE, "");
            if (!(date.isEmpty())) {
                StringBuilder datestr = new StringBuilder();
                datestr.append(date.split("/")[2]).append(date.split("/")[0]).append(date.split("/")[1]);
                params.add("begin_date", datestr.toString());
            }
            int sort_order = data.getInt(SORT_ORDER, 0);
            if (sort_order == 1)
                params.add("sort", "newest");
            else
                params.add("sort", "oldest");

            Boolean arts = data.getBoolean(ARTS, false);
            Boolean sports = data.getBoolean(SPORTS, false);
            Boolean fashion = data.getBoolean(FASHION, false);
            if (arts || sports || fashion) {
                params.add("fq", getnews_desk(arts, sports, fashion));
            }
            Log.i(TAG, params.toString());

                client.get(NYTSEARCH_URL, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i(TAG, "Search Success");


                        try {
                            JSONArray result = (JSONArray) response.getJSONObject("response").getJSONArray("docs");
                            articleList.addAll(Article.getArticleList(result));
                            adp.notifyItemRangeInserted(0, articleList.size());
                            Log.i(TAG, articleList.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                // searchbtn.setEnabled(true);

                                if(articleList.size()==0){
                                    nyGrid.setVisibility(View.GONE);
                                    empty.setVisibility(View.VISIBLE);
                                    empty.setText("No results found !! \uD83D\uDE1E  \uD83D\uDE1E");
                                }
                                else{
                                    nyGrid.setVisibility(View.VISIBLE);
                                    empty.setVisibility(View.GONE);
                                    Toast.makeText(mCtx, "Search Success", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.i(TAG, "Search Failed");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                               // searchbtn.setEnabled(true);
                                Toast.makeText(mCtx, "Failed to Search", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(mCtx, "Failed to Search", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if(!isOnline()) {
            searchItem.setEnabled(false);
        }
        else{
            searchItem.setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
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


    @Override
    public void networkAvailable() {
        Log.i(TAG,"Network is on");
        invalidateOptionsMenu();
    }

    @Override
    public void networkUnavailable() {
        Log.i(TAG,"Network is off");
        invalidateOptionsMenu();
    }

    @Override
    public void onFilterSaved(Bundle bundle) {
        data = bundle;
        //Log.i(NYTSearchActivity.TAG,"SORT = "+bundle.getInt(SORT_ORDER));
        //Log.i(TAG,"arts = "+bundle.getBoolean(ARTS)+" sports = "+bundle.getBoolean(SPORTS)+" fashion = "+bundle.getBoolean(FASHION));
    }
}
