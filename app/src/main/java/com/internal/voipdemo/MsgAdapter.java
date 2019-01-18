package com.internal.voipdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MsgAdapter extends BaseAdapter
{
    private List<MsgEntity> m_dataList;
    private Context m_context;

    public MsgAdapter(Context context, List<MsgEntity> data) {
        m_context = context;
        m_dataList = data;
    }
    public void setDate(List<MsgEntity> data){
        m_dataList = data;
    }
    @Override
    public int getCount() {
        return m_dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return m_dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(null == view) {
            view = LayoutInflater.from(m_context).inflate(R.layout.msg_item, null);
            holder = new ViewHolder();
            holder.accountid = (TextView) view.findViewById(R.id.tv_item_account);
            holder.result = (TextView) view.findViewById(R.id.tv_item_result);
            holder.state = (TextView) view.findViewById(R.id.tv_item_state);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        MsgEntity msgEntity = (MsgEntity)m_dataList.get(i);

        holder.accountid.setText(msgEntity.getMsgFromId());

        holder.result.setText(String.valueOf(msgEntity.getMsgStatus()));

        holder.state.setText(String.valueOf(msgEntity.getMsgId()));

        return view;
    }

    public class ViewHolder
    {
        private TextView accountid;
        private TextView result;
        private TextView state;
    }
}
