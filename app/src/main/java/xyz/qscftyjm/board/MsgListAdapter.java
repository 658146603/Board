package xyz.qscftyjm.board;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MsgListAdapter extends BaseAdapter implements View.OnClickListener {

    ArrayList<Msg> msgList;
    Context context;
    ViewHolder viewHolder;
    ItemMoreClickListener itemMoreClickListener;

    public MsgListAdapter(ArrayList<Msg> msgList, Context context){
        this.context=context;this.msgList=msgList;
    }

    @Override
    public int getCount() {
        return msgList.size();
    }

    @Override
    public Object getItem(int position) {
        return msgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            convertView = LayoutInflater.from(context).inflate(R.layout.msg_card, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.portrait.setImageBitmap(msgList.get(position).getPortrait());
        viewHolder.time.setText(msgList.get(position).getTime());
        viewHolder.nickname.setText(msgList.get(position).getNickname());
        viewHolder.content.setText(msgList.get(position).getContent());
        if(msgList.get(position).isHasPic()){
            viewHolder.picture.setVisibility(View.VISIBLE);
            viewHolder.picture.setImageBitmap(msgList.get(position).getPicture()[0]);
        }

        viewHolder.more.setOnClickListener(this);
        viewHolder.portrait.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        itemMoreClickListener.itemClick(v);
    }

    public void setOnInnerItemOnClickListener(ItemMoreClickListener listener){
        this.itemMoreClickListener=listener;
    }

    interface ItemMoreClickListener{
        void itemClick(View v);
    }

    class ViewHolder{
        ImageView portrait, picture, more;
        TextView time, nickname, content;

        public ViewHolder(View view) {
            portrait=view.findViewById(R.id.msg_head_portrait);
            picture=view.findViewById(R.id.msg_picture);
            more=view.findViewById(R.id.msg_more);
            time=view.findViewById(R.id.msg_time);
            nickname=view.findViewById(R.id.msg_nickname);
            content=view.findViewById(R.id.msg_content);

        }
    }


}
