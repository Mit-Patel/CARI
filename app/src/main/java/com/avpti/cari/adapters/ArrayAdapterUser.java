package com.avpti.cari.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.avpti.cari.R;
import com.avpti.cari.classes.Common;
import com.avpti.cari.classes.User;
import com.avpti.cari.services.BackgroundIntentService;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//adapter for the listview of the all users activity
public class ArrayAdapterUser extends ArrayAdapter<User> implements View.OnClickListener {

    //context of the calling activity
    Context context;
    User user;
    boolean showDelete;
    //receiver
    RemoveUserReceiver receiver;
    //object of arraylist of type user
    private ArrayList<User> list;

    //constructor
    public ArrayAdapterUser(Context context, ArrayList<User> data) {
        super(context, R.layout.all_users_list_item, data);
        this.list = data;
        this.context = context;
        this.showDelete = true;
        registerCustomReceiver();
    }

    public ArrayAdapterUser(Context context, ArrayList<User> data, boolean showDelete) {
        super(context, R.layout.all_users_list_item, data);
        this.list = data;
        this.context = context;
        this.showDelete = showDelete;
        registerCustomReceiver();
    }

    //show the items view in the list
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //get the user
        User user = getItem(position);
        //viewholder object
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.all_users_list_item, parent, false);

            viewHolder.tvId = convertView.findViewById(R.id.tv_id);
            viewHolder.tvName = convertView.findViewById(R.id.tv_name);
            viewHolder.btnDelete = convertView.findViewById(R.id.btn_delete);
            if (!showDelete) viewHolder.btnDelete.setVisibility(View.INVISIBLE);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (user != null) {
            viewHolder.tvId.setText(Integer.toString(user.getUser_id()));
            viewHolder.tvName.setText(user.getFname() + " " + user.getLname());
            if (showDelete) {
                viewHolder.btnDelete.setOnClickListener(this);
                viewHolder.btnDelete.setTag(position);
            }

        }
        return convertView;
    }

    //listview item button click event
    //user delete button click event
    @Override
    public void onClick(View v) {
        //get the user whose button was clicked
        user = getItem((Integer) v.getTag());
        Common.promptForResult(context, "Remove User", "Areyou sure to remove this user?","yes","no",true, new Common.PromptRunnable() {
            // put whatever code you want to run after user enters a result
            public void run() {
                if (this.getResult()) {
                    startBackgroundIntentService();
                }
            }
        });
    }

    private void registerCustomReceiver() {
        receiver = new RemoveUserReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundIntentService.CARI_ACTION_USER_DELETE);
        context.registerReceiver(receiver, intentFilter);
    }

    public void startBackgroundIntentService() {
        Intent intent = new Intent();
        intent.setClass(context, BackgroundIntentService.class);
        intent.setAction(BackgroundIntentService.CARI_ACTION_USER_DELETE);
        intent.putExtra("user_id", user.getUser_id());
        context.startService(intent);
    }

    //ViewHolder consists of the item views of the list
    private static class ViewHolder {
        TextView tvId;
        TextView tvName;
        ImageButton btnDelete;
    }

    class RemoveUserReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get the response of the server
            String res[] = intent.getStringArrayExtra(BackgroundIntentService.CARI_RPI);
            //checking if delete success or failed
            if (res[0].equals("9")) {
                list.remove(user);
                //acknowledgement
                Toast.makeText(context, "User deleted!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "User not deleted!", Toast.LENGTH_LONG).show();
            }
            ArrayAdapterUser.this.notifyDataSetChanged();

        }
    }
}
