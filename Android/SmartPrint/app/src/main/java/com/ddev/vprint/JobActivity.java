package com.ddev.vprint;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danish Shah on 10-07-2017.
 */

public class JobActivity extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar progressBar;
    RequestQueue requestQueue;
    ArrayList<HashMap<String, String>> list = new ArrayList<>();
    String printer_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        printer_name = getIntent().getExtras().getString("printer_name");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(printer_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        requestQueue = Volley.newRequestQueue(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        rv = (RecyclerView) findViewById(R.id.jobs_rv);

        getJobs(printer_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_job, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.refresh) {
            getJobs(printer_name);
        }
        return super.onOptionsItemSelected(item);
    }


    public void getJobs(final String printer_name){

        String url = "http://192.168.0.104:8000/jobs/";
        final ArrayList<HashMap<String, String>> job_list = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response!=null){
                    try {
                        JSONArray array = new JSONArray(response);

                        for (int i=0;i<array.length();i++){
                            JSONObject job = array.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<>();

                            String doc = job.getString("pDocument");
                            String status = job.getString("pStatus");
                            String position = job.getInt("Position")+"";
                            String total_pages= job.getInt("TotalPages")+"";
                            String pages_printer = job.getInt("PagesPrinted")+"";

                            map.put("document", doc);
                            map.put("status", status);
                            map.put("position", position);
                            map.put("total", total_pages);
                            map.put("current", pages_printer);

                            job_list.add(map);
                        }

                        rv.setAdapter(new JobAdapter(JobActivity.this, job_list));
                        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                        progressBar.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("printer_name", printer_name);

                return params;
            }
        };

        requestQueue.add(request);
    }

    class JobAdapter extends RecyclerView.Adapter<JobViewHolder> {

        ArrayList<HashMap<String, String>> list;
        Context cxt;

        public JobAdapter(Context cxt, ArrayList<HashMap<String, String>> list) {
            this.list = list;
            this.cxt = cxt;
        }

        @Override
        public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(cxt).inflate(R.layout.job, parent, false);
            return new JobViewHolder(v);
        }

        @Override
        public void onBindViewHolder(JobViewHolder holder, int position) {

            final HashMap<String, String> map = list.get(position);

            holder.position.setText(map.get("position") + "." );
            holder.name.setText(map.get("document"));
            holder.status.setText(map.get("status"));
            if (!map.get("status").equals("Printing")){
                holder.status.setTextColor(getResources().getColor(R.color.offline));
            }
            holder.pages.setText(map.get("current") + "/" + map.get("total"));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class JobViewHolder extends RecyclerView.ViewHolder{

        CardView card;
        TextView position, name, status, pages;

        public JobViewHolder(View itemView) {
            super(itemView);

            position = (TextView) itemView.findViewById(R.id.position);
            name = (TextView) itemView.findViewById(R.id.file_name);
            status = (TextView) itemView.findViewById(R.id.status);
            pages = (TextView) itemView.findViewById(R.id.page_print);
            card = (CardView) itemView.findViewById(R.id.card_print);
        }
    }
}
