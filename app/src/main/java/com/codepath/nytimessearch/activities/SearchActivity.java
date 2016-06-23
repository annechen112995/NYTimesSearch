package com.codepath.nytimessearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.nytimessearch.Article;
import com.codepath.nytimessearch.ArticleArrayAdapter;
import com.codepath.nytimessearch.EndlessRecyclerViewScrollListener;
import com.codepath.nytimessearch.ItemClickSupport;
import com.codepath.nytimessearch.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity {

    RecyclerView rvResults;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

    ShareActionProvider miShareAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Lookup the recyclerview in activity layout
        RecyclerView rvResults = (RecyclerView) findViewById(R.id.rvResults);

        /** if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mRecycler.setLayoutManager(new GridLayoutManager(mContext, 2));
        }
        else{
            mRecycler.setLayoutManager(new GridLayoutManager(mContext, 4));
        } */

        // Create adapter passing in the sample user data
        ArticleArrayAdapter adapter = new ArticleArrayAdapter(this, articles);

        // Attach the adapter to the recyclerview to populate items
        assert rvResults != null;
        rvResults.setAdapter(adapter);

        // Set layout manager to position the items
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvResults.setLayoutManager(linearLayoutManager);

        // Add the scroll listener
        rvResults.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                customLoadMoreDataFromApi(page);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setListener();
    }

    // Append more data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void customLoadMoreDataFromApi(int page) {
        // Send an API request to retrieve appropriate data using the offset value as a parameter.
        //  --> Deserialize API response and then construct new objects to append to the adapter
        //  --> Notify the adapter of the changes
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem item = menu.findItem(R.id.menu_item_share);

        miShareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchItem.expandActionView();
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                articles.clear();

                //perform query here
                fetchResults(query);

                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        int id = item.getItemId();

        if (id == R.id.menu_item_share) {
            fetchShare(item);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.menu_item_share) {
            fetchShare(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetchResults(String query) {

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.nytimes.com/svc/search/v2/articlesearch.json";

        RequestParams params = new RequestParams();
        params.put("api-key", "4706784a04264dae9fe15aabaf3cd2ae");
        params.put("page", 0);
        params.put("q", query);

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray articleJSONResults = null;

                try {
                    articleJSONResults = response.getJSONObject("response").getJSONArray("docs");
                    articles.addAll(Article.fromJsonArray(articleJSONResults));
                    adapter.notifyDataSetChanged();
                    Log.d("DEBUG", articles.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setListener() {
        rvResults = (RecyclerView) findViewById(R.id.rvResults);
        articles = new ArrayList<>();
        adapter = new ArticleArrayAdapter(this, articles);
        rvResults.setAdapter(adapter);

        //Allows action when grid is clicked
        ItemClickSupport.addTo(rvResults).setOnItemClickListener(
            new ItemClickSupport.OnItemClickListener() {

                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                //Creates an intent to display article
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);

                //Gets article to display
                Article article = articles.get(position);

                //Pass article into intent
                i.putExtra("article", article);

                //Starts activity
                startActivity(i);
            }
        });
    }

    public void fetchShare(MenuItem item) {

        // Return true to display menu
        // return true;
        ShareActionProvider miShare = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // get reference to WebView
        // WebView wvArticle = (WebView) findViewById(R.id.wvArticle);

        miShare.setShareIntent(shareIntent);
    }
}
