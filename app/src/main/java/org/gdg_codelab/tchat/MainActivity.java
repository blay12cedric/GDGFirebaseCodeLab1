package org.gdg_codelab.tchat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.FirebaseListAdapter;
import org.gdg_codelab.tchat.model.Message;

import java.util.HashMap;

/**
 * Created by setico on 22/11/15.
 */

public class MainActivity extends AppCompatActivity {
    private Firebase backend;
    private EditText ed_message;
    private ListView list;
    private FirebaseListAdapter listAdapter;
    private String user;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        backend = new Firebase(Config.BACKEND_URL);
        setContentView(R.layout.activity_main);
        ed_message = (EditText) findViewById(R.id.message);
        list = (ListView) findViewById(R.id.list);


        backend.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    user = ((String) authData.getProviderData().get(getString(R.string.email_key))).split("@")[0];
                    ((Button) findViewById(R.id.login)).setVisibility(View.GONE);
                    ((LinearLayout)findViewById(R.id.bot_layout)).setVisibility(View.VISIBLE);
                    listAdapter = new FirebaseListAdapter(MainActivity.this, Message.class, R.layout.list_item, backend.child(Config.MESSAGE_CHILD)) {
                        @Override
                        protected void populateView(View view, Object o) {
                            ViewHolder viewHolder = new ViewHolder(view);
                            Message message = (Message) o;
                            viewHolder.user.setText(message.getUser() + ": ");
                            viewHolder.message.setText(message.getMessage());
                        }
                    };
                    list.setAdapter(listAdapter);
                    if(loading!=null)
                        loading.dismiss();
                } else {
                    user = null;
                    ((Button) findViewById(R.id.login)).setVisibility(View.VISIBLE);
                    ((LinearLayout)findViewById(R.id.bot_layout)).setVisibility(View.GONE);
                    list.setAdapter(null);
                }
            }
        });
    }

    public static class ViewHolder{
        TextView user;
        TextView message;

        public ViewHolder(View v){
            this.user = (TextView)v.findViewById(R.id.user);
            this.message = (TextView)v.findViewById(R.id.message);
        }

    }

    public void envoyer(View v){
        if(user!=null) {
            HashMap<String, String> map = new HashMap<>();
            backend.child(Config.MESSAGE_CHILD).push().setValue(new Message(user,ed_message.getText().toString()));
            ed_message.setText("");
        }else
            Toast.makeText(this, getString(R.string.toast_user_not_connected), Toast.LENGTH_SHORT).show();
    }

    public void login(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.create();
        dialog.setTitle(getString(R.string.dialog_connexion_title));
        dialog.setView(getLayoutInflater().inflate(R.layout.dialog_connexion, null));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_connexion_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                final String email = ((EditText) ((Dialog) dialog).findViewById(R.id.email)).getText().toString();
                final String passsword = ((EditText) ((Dialog) dialog).findViewById(R.id.password)).getText().toString();
                loading = new ProgressDialog(MainActivity.this);
                loading.setMessage(getString(R.string.dialog_connexion_loading));
                loading.setIndeterminate(true);
                loading.setCancelable(false);
                loading.show();
                backend.createUser(email, passsword, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        backend.authWithPassword(email, passsword, null);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        backend.authWithPassword(email, passsword, null);
                        dialog.dismiss();
                    }
                });

            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(org.gdg_codelab.tchat.R.string.dialog_connexion_negative_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
    }
}
