package com.ddev.vprint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

/**
 * Created by Danish Shah on 09-07-2017.
 */

public class PrintActivity extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar progressBar;
    RequestQueue requestQueue;

    static String printer_name;
    ArrayList<String> filePaths = new ArrayList<>();
    ArrayList<String> docPaths;
    String file_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Printers");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        requestQueue = Volley.newRequestQueue(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        rv = (RecyclerView) findViewById(R.id.printer_rv);
        if (new NetworkUtils().checkNetwork(this))
            new checkConnection().execute();
        else {
            Toast.makeText(PrintActivity.this, "No Connection, Try again later", Toast.LENGTH_SHORT).show();
            finish();
        }

        getPrinter();

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
            getPrinter();
        }
        return super.onOptionsItemSelected(item);
    }

    class checkConnection extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try{

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("192.168.0.106", 8000), 5000);
                if (socket.isConnected()) {
                    System.out.println("Connected");
                    socket.close();
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Not Connected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PrintActivity.this, "Server Offline, Please try again later", Toast.LENGTH_SHORT).show();
                    }
                });

                finish();
            }
            return null;
        }
    }

    public void getPrinter(){

        final ArrayList<HashMap<String, String>> list = new ArrayList<>();

        String url = "http://192.168.0.106:8000/printers/";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response!=null){
                    try {
//                        System.out.println(response);
                        JSONObject main = new JSONObject(response);
                        JSONArray offline = main.getJSONArray("Offline");
                        JSONArray online = main.getJSONArray("Online");

                        HashMap<String, String> map = new HashMap<>();


                        for (int i=0;i<offline.length();i++){
                            map.put(offline.getString(i), "Offline");
                            System.out.println(offline.getString(i));
                        }

                        for (int i=0;i<online.length();i++){
                            map.put(online.getString(i), "Online");
                            System.out.println(online.getString(i));
                        }

                        list.add(map);
//                        System.out.println(list.size());
                        rv.setAdapter(new PrinterAdapter(PrintActivity.this, list));
                        rv.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
                        progressBar.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(PrintActivity.this, "Server Offline", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PrintActivity.this, error.toString(), Toast.LENGTH_SHORT);
            }
        });

        requestQueue.add(stringRequest);
    }

    class PrinterAdapter extends RecyclerView.Adapter<PrinterViewHolder>{

        ArrayList<HashMap<String, String>> list;
        Context cxt;

        public PrinterAdapter(Context cxt, ArrayList<HashMap<String, String>> list) {
            this.list = list;
            this.cxt = cxt;
        }

        @Override
        public PrinterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(cxt).inflate(R.layout.pritner, parent, false);
            return new PrinterViewHolder(v);
        }

        @Override
        public void onBindViewHolder(PrinterViewHolder holder, int position) {

            final HashMap<String, String> map = list.get(position);
            for ( final String key : map.keySet() ) {
                holder.name.setText(key);
                holder.status.setText(map.get(key));
                if (map.get(key).equals("Offline")){
                    holder.img.setColorFilter(getResources().getColor(R.color.disable));
                    holder.card.setCardBackgroundColor(getResources().getColor(R.color.offline));
                }


                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (map.get(key).equals("Online")) {
                            printer_name = key;

                            setPrinter(printer_name);
                            int rc = ActivityCompat.checkSelfPermission(PrintActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                            if (rc == PackageManager.PERMISSION_GRANTED) {
                                FilePickerBuilder.getInstance().setMaxCount(1)
                                        .setSelectedFiles(filePaths)
                                        .setActivityTheme(R.style.AppTheme)
                                        .pickDocument(PrintActivity.this);
                            } else {
                                requestReadStoragePermission();
                            }
                        }else {
                            Toast.makeText(PrintActivity.this, "Printer is Offline, Try again later", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        printer_name = key;
                        System.out.println(printer_name);
                        Intent job = new Intent(PrintActivity.this, JobActivity.class);
                        job.putExtra("printer_name", printer_name);
                        startActivity(job);
                    }
                });
            }


        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class PrinterViewHolder extends RecyclerView.ViewHolder{

        CardView card;
        ImageView img;
        TextView name, status;
        Button btn;

        public PrinterViewHolder(View itemView) {
            super(itemView);

            img = (ImageView) itemView.findViewById(R.id.printer_img);
            name = (TextView) itemView.findViewById(R.id.printer_name);
            status = (TextView) itemView.findViewById(R.id.printer_status);
            card = (CardView) itemView.findViewById(R.id.card_print);
            btn = (Button) itemView.findViewById(R.id.view_job);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    docPaths = new ArrayList<>();
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                }
                break;
        }
        if (docPaths != null) {
            if (docPaths.size() > 0) {
                File file = new File(docPaths.get(0));
                int length = Integer.parseInt(String.valueOf(file.length() / 1024));
                if (length < 4000) {
                    String file_dir[] = docPaths.get(0).split("/");
                    int count = file_dir.length;
                    file_name = file_dir[count - 1];

                    filePaths.addAll(docPaths);
                    new uploadFiles().execute();
                } else {
                    Toast.makeText(PrintActivity.this, "File should be less than 4MB", Toast.LENGTH_SHORT).show();
                    docPaths = new ArrayList<>();
                }
            }
        }
    }

    public void requestReadStoragePermission() {
        final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(PrintActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(PrintActivity.this, permissions, 4);
            return;
        }

        final Activity Activity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(PrintActivity.this, permissions, 4);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode != 4) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            FilePickerBuilder.getInstance().setMaxCount(1)
                    .setSelectedFiles(filePaths)
                    .setActivityTheme(R.style.AppTheme)
                    .pickDocument(this);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            FilePickerBuilder.getInstance().setMaxCount(1)
                    .setSelectedFiles(filePaths)
                    .setActivityTheme(R.style.AppTheme)
                    .pickDocument(this);

            return;
        }

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                requestReadStoragePermission();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(PrintActivity.this);
        builder.setTitle("Permission Denied")
                .setMessage("Read External Storage Permission Denied")
                .setPositiveButton("GRANT", listener)
                .setNegativeButton("CANCEL", null)
                .show();
    }

    public void setPrinter(final String printer_name){
        String url = "http://192.168.0.106:8000/set_printer/";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
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



    class uploadFiles extends AsyncTask<Void, Void, Void> {

        String response = null;

        @Override
        protected Void doInBackground(Void... voids) {

            final String uploadFilePath = filePaths.get(0);
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int serverResponseCode = 0;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 4 * 1024 * 1024;
            File sourceFile = new File(uploadFilePath);

            if (!sourceFile.isFile()) {

                Log.e("uploadFile", "Source File not exist :"
                        + uploadFilePath + "" + file_name);

                return null;

            } else {
                try {

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL("http://192.168.0.106:8000/upload/");

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//                    conn.setRequestProperty("printer_name", printer_name);

                    dos = new DataOutputStream(conn.getOutputStream());
                    System.out.println(file_name);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
                            + file_name + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response = br.readLine();

//                    OutputStream os = conn.getOutputStream();
//                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//                    StringBuilder op = new StringBuilder(0);
//                    op.append(URLEncoder.encode("printer_name", "UTF-8"));
//                    op.append("=");
//                    op.append(URLEncoder.encode(printer_name, "UTF-8"));
//                    writer.write(op.toString());


                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(PrintActivity.this, response, Toast.LENGTH_LONG).show();
                            }

                        });
                    }

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
//                    writer.flush();
//                    writer.close();
//                    os.close();

                } catch (MalformedURLException ex) {

                    ex.printStackTrace();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(PrintActivity.this, "MalformedURLException",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {

                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(PrintActivity.this, "Got Exception : see logcat ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent job = new Intent(PrintActivity.this, JobActivity.class);
            job.putExtra("printer_name", printer_name);
            startActivity(job);
        }
    }

}
