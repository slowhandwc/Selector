package com.slow.selector;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.slow.selector.adapter.SelectEntitiesAdapter;
import com.slow.selector.model.SelectEntity;
import com.slow.selector.view.BottomDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择器
 *
 * @Author wuchao
 * @Date 2020/4/8-11:16 PM
 * @description
 * @email 329187218@qq.com
 * @see
 */
public class Selector {
    /**
     * 最深的层级数
     */
    private final int mMaxLevel;
    private final Context mContext;
    private String mTitle;
    private String mNameDivider;
    private BottomDialog mBottomDialog;
    private final TabLayout mTabLayout;
    private RecyclerView mRecyclerView;
    private SelectEntitiesAdapter mAdapter;
    private List<SelectEntity> selectedEntities = new ArrayList<>();
    private SelectorEntitiesProviderCallback mSelectorEntitiesProviderCallback;
    private SelectEntity mSelectorEntitiesRootCache;

    private Selector(Builder builder) {
        this.mSelectorEntitiesRootCache = new SelectEntity();
        this.mContext = builder.context;
        this.mBottomDialog = new BottomDialog.Builder(mContext).setContentViewResourceId(R.layout.widget_bottom_selector).create();
        this.mTitle = builder.title;
        this.mNameDivider = builder.nameDivider;
        this.mMaxLevel = builder.maxLevel;
        this.mSelectorEntitiesProviderCallback = builder.selectorEntitiesProviderCallback;
        TextView titleView = mBottomDialog.findViewById(R.id.tvTitle);
        titleView.setText(mTitle);
        this.mTabLayout = mBottomDialog.findViewById(R.id.tbLayout);
        this.mRecyclerView = mBottomDialog.findViewById(R.id.recyclerView);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        this.mAdapter = new SelectEntitiesAdapter(new ArrayList<SelectEntity>());
        this.mRecyclerView.setAdapter(mAdapter);
        this.mAdapter.setOnItemClickListener(new SelectEntitiesAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position, SelectEntity selectEntity) {
                if(selectedEntities.size()>0){
                    List<SelectEntity> needDeleteList = new ArrayList<>();
                    for (int i = 0; i < selectedEntities.size(); i++) {
                        SelectEntity item = selectedEntities.get(i);
                        if (item.getLevel() >= selectEntity.getLevel()) {
                            needDeleteList.add(item);
                        }
                    }
                    if (needDeleteList.size() > 0) {
                        selectedEntities.removeAll(needDeleteList);
                    }
                }
                selectedEntities.add(selectEntity);
                if (selectEntity.getLevel() >= mMaxLevel) {
                    dissmiss();
                } else {
                    //update checked status
                    TabLayout.Tab currentTab = getTab(selectEntity.getLevel() - 1);
                    if(currentTab!=null){
                        currentTab.setText(selectEntity.getName());
                    }
                    if (mTabLayout.getTabCount() <= selectEntity.getLevel()) {
                        addNewTab("请选择",selectEntity,true);
                    } else {
                        removeTabIfMoreThan(selectEntity.getLevel() + 1);
                        TabLayout.Tab nextTab = getTab(mTabLayout.getTabCount() - 1);
                        nextTab.setText("请选择");
                        nextTab.setTag(selectEntity);
                        mTabLayout.selectTab(nextTab);
                    }
                }
            }
        });
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                SelectEntity selectEntity = (SelectEntity) tab.getTag();
                if(selectEntity!=null){
                    if(selectEntity.isHaveChildren()){
                        if("请选择".equals(tab.getText())){
                            for(SelectEntity item:selectEntity.getChildrenEntities()){
                                item.setChecked(false);
                            }
                        } else {
                            for (SelectEntity item:selectEntity.getChildrenEntities()){
                                if(selectedEntities.contains(item)){
                                    item.setChecked(true);
                                }
                            }
                        }
                        mAdapter.setNewData(selectEntity.getChildrenEntities());
                    } else {
                        List<SelectEntity> cachedNeededList = mSelectorEntitiesRootCache.getSelectNodeChildren(selectEntity.getLevel(),selectEntity.getId());
                        if(cachedNeededList!=null){
                            if("请选择".equals(tab.getText())){
                                for(SelectEntity item:cachedNeededList){
                                    item.setChecked(false);
                                }
                            } else {
                                for (SelectEntity item:cachedNeededList){
                                    if(selectedEntities.contains(item)){
                                        item.setChecked(true);
                                    }
                                }
                            }
                            mAdapter.setNewData(cachedNeededList);
                        } else {
                            sendProviderDemand(selectEntity.getLevel() + 1, selectEntity);
                        }
                    }
                } else {
                    sendProviderDemand(selectEntity.getLevel() + 1, selectEntity);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if(builder.defaultSelectEntities!=null&&!builder.defaultSelectEntities.isEmpty()){
            selectedEntities.addAll(builder.defaultSelectEntities);
            for(int index=0;index<selectedEntities.size();index++){
                SelectEntity selectEntity = selectedEntities.get(index);
                if(index == 0){
                    mSelectorEntitiesRootCache.setId(selectEntity.getParentId());
                    addNewTab(selectEntity.getName(),mSelectorEntitiesRootCache,false);
                } else {
                    addNewTab(selectEntity.getName(),selectedEntities.get(index -1),false);
                }
            }
            mTabLayout.selectTab(getTab(mTabLayout.getTabCount()-1));
        }
        ImageView close = mBottomDialog.findViewById(R.id.ivClose);
        close.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                dissmiss();
            }
        });
    }

    public void show() {
        //加载第一级数据
        mBottomDialog.show();
        if(selectedEntities.isEmpty()){
            sendProviderDemand(1, null);
        }
    }

    public void dissmiss() {
        mSelectorEntitiesProviderCallback.onEntitiesSelected(selectedEntities, getSelectEntitesWholeName());
        mSelectorEntitiesProviderCallback = null;
        mBottomDialog.dismiss();
    }

    private TabLayout.Tab getTab(int index){
        TabLayout.Tab tab = mTabLayout.getTabAt(index);
        return tab;
    }

    private void addNewTab(String tabName,SelectEntity parent,boolean isSelected) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(tabName);
        tab.setTag(parent);
        mTabLayout.addTab(tab,isSelected);
    }

    private void removeTabIfMoreThan(int moreThan) {
        if (mTabLayout.getTabCount() > moreThan) {
            mTabLayout.removeTabAt(mTabLayout.getTabCount() - 1);
            removeTabIfMoreThan(moreThan);
        }
    }

    private void sendProviderDemand(int level, SelectEntity parentEntity) {
        mSelectorEntitiesProviderCallback.onEntitiesProvide(level, parentEntity, mSelectorEntitiesProvider);
    }

    private ISelectorEntitiesProvider mSelectorEntitiesProvider = new ISelectorEntitiesProvider() {
        @Override
        public void sendEntities(List<SelectEntity> dataList) {
            if (dataList.size() > 0) {
                if(mSelectorEntitiesRootCache.getId().isEmpty()){
                    mSelectorEntitiesRootCache.setId(dataList.get(0).getParentId());
                    addNewTab("请选择",mSelectorEntitiesRootCache,true);
                }
                mSelectorEntitiesRootCache.setNodes(dataList);
                for(SelectEntity selectEntity:dataList){
                    if(selectedEntities.contains(selectEntity)){
                        selectEntity.setChecked(true);
                    }
                }
                mAdapter.setNewData(dataList);
            } else {
                mSelectorEntitiesProviderCallback.onEntitiesSelected(selectedEntities, getSelectEntitesWholeName());
            }
        }
    };

    public String getSelectEntitesWholeName() {
        StringBuilder stringBuilder = new StringBuilder();
        for (SelectEntity selectEntity : selectedEntities) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(mNameDivider);
            }
            stringBuilder.append(selectEntity.getName());
        }
        return stringBuilder.toString();
    }


    public interface SelectorEntitiesProviderCallback {
        /**
         * 提供选择数据
         * 组件调用此方法，将ISelectorEntitiesProvider 回调传给业务界面
         * 业务界面在获取数据之后，通过ISelectorEntitiesProvider 的 sendEntities 方法将数据发送回来
         * @param level 层级
         * @param parentSelectEntity 父级 SelectEntity
         * @param selectorEntitiesProvider 选择数据提供者接口
         */
        void onEntitiesProvide(int level, SelectEntity parentSelectEntity, ISelectorEntitiesProvider selectorEntitiesProvider);

        /**
         * 选择完成
         * @param selectEntityList 已选择的数据
         * @param selectEntitiesWholeName 已选择的数据名称的格式化字符串
         */
        void onEntitiesSelected(List<SelectEntity> selectEntityList, String selectEntitiesWholeName);
    }

    public interface ISelectorEntitiesProvider {
        void sendEntities(List<SelectEntity> dataList);
    }

    public static class Builder {
        private Context context;
        private int maxLevel;
        private String title;
        private String nameDivider;
        private List<SelectEntity> defaultSelectEntities;
        private SelectorEntitiesProviderCallback selectorEntitiesProviderCallback;

        public Builder(Context context) {
            this.context = context;
            this.nameDivider = "-";
            this.maxLevel = 4;
        }

        public Builder setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(int resourceId) {
            this.title = context.getString(resourceId);
            return this;
        }

        public Builder setNameDivider(String nameDivider) {
            this.nameDivider = nameDivider;
            return this;
        }

        public Builder setDefaultEntities(List<SelectEntity> selectEntities){
            this.defaultSelectEntities = selectEntities;
            return this;
        }

        public Builder setOnSelectorEntitiesProviderCallback(SelectorEntitiesProviderCallback selectorEntitiesProviderCallback) {
            this.selectorEntitiesProviderCallback = selectorEntitiesProviderCallback;
            return this;
        }

        public Selector create() {
            return new Selector(this);
        }
    }
}
