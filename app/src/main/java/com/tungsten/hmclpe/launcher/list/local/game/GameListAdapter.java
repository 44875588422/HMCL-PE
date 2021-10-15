package com.tungsten.hmclpe.launcher.list.local.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tungsten.hmclpe.R;
import com.tungsten.hmclpe.launcher.MainActivity;
import com.tungsten.hmclpe.launcher.manifest.AppManifest;
import com.tungsten.hmclpe.utils.gson.GsonUtils;
import com.tungsten.hmclpe.utils.resources.DrawableUtils;

import java.util.ArrayList;

public class GameListAdapter extends BaseAdapter {

    private Context context;
    private MainActivity activity;
    private ArrayList<GameListBean> list;

    private class ViewHolder{
        LinearLayout item;
        RadioButton radioButton;
        ImageView icon;
        TextView name;
        TextView version;
        ImageButton startGame;
        ImageButton moreVert;
    }

    public GameListAdapter(Context context,MainActivity activity,ArrayList<GameListBean> list){
        this.context = context;
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_local_version,null);
            viewHolder.item = convertView.findViewById(R.id.local_version_item);
            viewHolder.radioButton = convertView.findViewById(R.id.select_version);
            viewHolder.icon = convertView.findViewById(R.id.version_icon);
            viewHolder.name = convertView.findViewById(R.id.version_name);
            viewHolder.version = convertView.findViewById(R.id.version_id);
            viewHolder.startGame = convertView.findViewById(R.id.test_game);
            viewHolder.moreVert = convertView.findViewById(R.id.more_vert);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        if ((activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name).equals(activity.publicGameSetting.currentVersion)){
            list.get(position).isSelected = true;
        }
        if (list.get(position).isSelected){
            viewHolder.radioButton.setChecked(true);
        }
        else {
            viewHolder.radioButton.setChecked(false);
        }
        viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!list.get(position).isSelected){
                    for (int i = 0;i < list.size();i++){
                        if (i == position){
                            list.get(i).isSelected = true;
                        }
                        else {
                            list.get(i).isSelected = false;
                        }
                    }
                    activity.publicGameSetting.currentVersion = activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name;
                    GsonUtils.savePublicGameSetting(activity.publicGameSetting, AppManifest.SETTING_DIR + "/public_game_setting.json");
                    notifyDataSetChanged();
                }
            }
        });
        if (!list.get(position).iconPath.equals("")){
            viewHolder.icon.setImageDrawable(DrawableUtils.getDrawableFromFile(list.get(position).iconPath));
        }
        viewHolder.name.setText(list.get(position).name);
        viewHolder.version.setText(list.get(position).version);
        viewHolder.startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        viewHolder.moreVert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }
}
