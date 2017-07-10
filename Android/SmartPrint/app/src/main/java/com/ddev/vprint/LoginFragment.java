package com.ddev.vprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Home on 10/01/2017.
 */

public class LoginFragment extends Fragment {

    EditText u_name, pass;
    RequestQueue queue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);


        TextView logo = (TextView) v.findViewById(R.id.logo);

        queue = Volley.newRequestQueue(getContext());

        u_name = (EditText) v.findViewById(R.id.user_name);
        pass = (EditText) v.findViewById(R.id.password);

        Button btn = (Button) v.findViewById(R.id.login_btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (validateForm()){
                    sendData();
                //}
            }
        });

        return v;
    }

    public void sendData(){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://www.socialize.16mb.com/Socialize/include/login.php?user_name="+u_name.getText().toString()+"&pass="+pass.getText().toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch(response){
                            case "Verified":
                                startActivity(new Intent(getContext(), MainActivity.class));
                                break;
                            case "Password Incorrect":
                                pass.setError("Password Incorrect");
                                break;
                            case "Username Not Valid":
                                u_name.setError("User Doesn't Exist, try signing up");
                                break;
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("user_name",u_name.getText().toString());
                params.put("pass",pass.getText().toString());
                return params;
            }

        };

        queue.add(stringRequest);
    }
}
