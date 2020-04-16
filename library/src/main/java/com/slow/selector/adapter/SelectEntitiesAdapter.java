package com.slow.selector.adapter;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.slow.selector.R;
import com.slow.selector.model.SelectEntity;

import java.util.List;

/**
 * 选择实体适配器
 * @Author wuchao
 * @Date 2020/4/8-11:25 PM
 * @description
 * @email 329187218@qq.com
 * @see
 */
public class SelectEntitiesAdapter extends RecyclerView.Adapter {
    private int mCustomCheckedColor;
    private List<SelectEntity> mDataList;
    private OnItemClickListener mOnItemClickListener;

    public SelectEntitiesAdapter(List<SelectEntity> dataList) {
        mDataList = dataList;
    }

    public SelectEntitiesAdapter(List<SelectEntity> dataList,int customCheckedColor) {
        mDataList = dataList;
        mCustomCheckedColor = customCheckedColor;
    }

    public void checkOne(int position){
        for(SelectEntity selectEntity:mDataList){
            selectEntity.setChecked(false);
        }
        mDataList.get(position).setChecked(true);
    }

    public SelectEntity getItem(int position){
        return mDataList.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewHolderView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select,null);
        final SelectViewHolder selectViewHolder = new SelectViewHolder(viewHolderView);
        selectViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClick(v,selectViewHolder.getAdapterPosition(),getItem(selectViewHolder.getAdapterPosition()));
            }
        });
        selectViewHolder.mTitleView.setCompoundDrawableTintList(ColorStateList.valueOf(mCustomCheckedColor));
        return selectViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SelectEntity selectEntity = mDataList.get(position);
        SelectViewHolder selectViewHolder = (SelectViewHolder) holder;
        selectViewHolder.bindData(selectEntity);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void setNewData(List<SelectEntity> dataList){
        mDataList = dataList;
        notifyDataSetChanged();
    }

    class SelectViewHolder extends RecyclerView.ViewHolder{
        private TextView mTitleView;

        SelectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mTitleView = itemView.findViewById(R.id.tvSelectItem);
        }

        void bindData(SelectEntity selectEntity){
            mTitleView.setText(selectEntity.getName());
            if(selectEntity.isChecked()){
                mTitleView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.ic_check,0,0,0);
                mTitleView.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                mTitleView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                mTitleView.setTypeface(Typeface.DEFAULT);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }


    public interface OnItemClickListener{
        void onClick(View view,int position,SelectEntity selectEntity);
    }
}
