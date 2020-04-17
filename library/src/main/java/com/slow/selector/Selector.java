package com.slow.selector;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.slow.selector.adapter.SelectEntitiesAdapter;
import com.slow.selector.model.SelectEntity;
import com.slow.selector.view.BottomDialog;
import com.slow.selector.view.LoadingDialog;

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
    public static final String TAG = "SELECTOR";
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
    private Dialog mLoading;

    private Selector(Builder builder) {
        this.mSelectorEntitiesRootCache = new SelectEntity("0");
        if(builder.childrenOfRoot != null){
            mSelectorEntitiesRootCache.setChildrenEntities(builder.childrenOfRoot);
        }
        this.mContext = builder.context;
        if (builder.loadingDialog != null) {
            this.mLoading = builder.loadingDialog;
        } else {
            this.mLoading = new LoadingDialog(mContext);
        }
        this.mBottomDialog = new BottomDialog.Builder(mContext).setContentViewResourceId(R.layout.widget_bottom_selector).create();
        this.mTitle = builder.title;
        this.mNameDivider = builder.nameDivider;
        this.mMaxLevel = builder.maxLevel;
        this.mSelectorEntitiesProviderCallback = builder.selectorEntitiesProviderCallback;
        TextView titleView = mBottomDialog.findViewById(R.id.tvTitle);
        titleView.setText(mTitle);
        this.mTabLayout = mBottomDialog.findViewById(R.id.tbLayout);
        if (builder.customColor != 0) {
            this.mTabLayout.setSelectedTabIndicatorColor(builder.customColor);
        }
        this.mRecyclerView = mBottomDialog.findViewById(R.id.recyclerView);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        if (builder.customColor != 0) {
            this.mAdapter = new SelectEntitiesAdapter(new ArrayList<SelectEntity>(), builder.customColor);
        } else {
            this.mAdapter = new SelectEntitiesAdapter(new ArrayList<SelectEntity>());
        }
        this.mRecyclerView.setAdapter(mAdapter);
        this.mAdapter.setOnItemClickListener(new SelectEntitiesAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position, SelectEntity selectEntity) {
                if (selectedEntities.size() > 0) {
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
                    TabLayout.Tab currentTab = getTab(selectEntity.getLevel() - 1);
                    if (currentTab != null) {
                        currentTab.setText(selectEntity.getName());
                    }
                    if (mTabLayout.getTabCount() <= selectEntity.getLevel()) {
                        addNewTab(mContext.getString(R.string.please_choose), selectEntity, true);
                    } else {
                        removeTabIfMoreThan(selectEntity.getLevel() + 1);
                        TabLayout.Tab nextTab = getTab(mTabLayout.getTabCount() - 1);
                        nextTab.setText(mContext.getString(R.string.please_choose));
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
                if (selectEntity != null) {
                    if (selectEntity.isHaveChildren()) {
                        List<SelectEntity> currentSelectList = checkSelectedEntity(selectEntity.getChildrenEntities());
                        mAdapter.setNewData(currentSelectList);
                    } else {
                        List<SelectEntity> cachedNeededList = mSelectorEntitiesRootCache.getSelectNodeChildren(selectEntity.getLevel(), selectEntity.getId());
                        if (cachedNeededList != null) {
                            List<SelectEntity> currentSelectList = checkSelectedEntity(cachedNeededList);
                            mAdapter.setNewData(currentSelectList);
                        } else {
                            if (!selectedEntities.isEmpty()) {
                                List<SelectEntity> currentSelectList;
                                List<SelectEntity> selectedCacheList = selectedEntities.get(0).getSelectNodeChildren(selectEntity.getLevel(), selectEntity.getId());
                                if (selectedCacheList != null) {
                                    currentSelectList = checkSelectedEntity(selectedCacheList);
                                    mAdapter.setNewData(currentSelectList);
                                } else {
                                    sendProviderDemand(selectEntity.getLevel() + 1, selectEntity);
                                }
                            } else {
                                sendProviderDemand(selectEntity.getLevel() + 1, selectEntity);
                            }
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
        if (builder.defaultSelectEntities != null && !builder.defaultSelectEntities.isEmpty()) {
            selectedEntities.addAll(builder.defaultSelectEntities);
            for (int index = 0; index < selectedEntities.size(); index++) {
                SelectEntity selectEntity = selectedEntities.get(index);
                if (index == 0) {
                    mSelectorEntitiesRootCache.setId(selectEntity.getParentId());
                    addNewTab(selectEntity.getName(), mSelectorEntitiesRootCache, false);
                } else {
                    addNewTab(selectEntity.getName(), selectedEntities.get(index - 1), false);
                }
            }
            if (mTabLayout.getTabCount() < mMaxLevel) {
                addNewTab(mContext.getString(R.string.please_choose), selectedEntities.get(selectedEntities.size() - 1), true);
            } else {
                mTabLayout.selectTab(getTab(mTabLayout.getTabCount() - 1));
            }
        }
        ImageView close = mBottomDialog.findViewById(R.id.ivClose);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dissmiss();
            }
        });
    }

    private List<SelectEntity> checkSelectedEntity(List<SelectEntity> source) {
        List<SelectEntity> currentSelectEntities = new ArrayList<>();
        for (SelectEntity selectEntity : source) {
            SelectEntity copy = new SelectEntity(selectEntity.getName(), selectEntity.getId(), selectEntity.getParentId(), selectEntity.getLevel());
            currentSelectEntities.add(copy);
        }
        for (SelectEntity selectEntity : currentSelectEntities) {
            if (selectedEntities.contains(selectEntity)) {
                selectEntity.setChecked(true);
                break;
            }
        }
        return currentSelectEntities;
    }

    public void show() {
        //加载第一级数据
        mBottomDialog.show();
        if (selectedEntities.isEmpty()) {
            sendProviderDemand(1, null);
        }
    }

    public void dissmiss() {
        mSelectorEntitiesProviderCallback.onEntitiesSelected(selectedEntities, getSelectEntitesWholeName());
        mSelectorEntitiesProviderCallback = null;
        mBottomDialog.dismiss();
    }

    private TabLayout.Tab getTab(int index) {
        TabLayout.Tab tab = mTabLayout.getTabAt(index);
        return tab;
    }

    private void addNewTab(String tabName, SelectEntity parent, boolean isSelected) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(tabName);
        tab.setTag(parent);
        mTabLayout.addTab(tab, isSelected);
    }

    private void removeTabIfMoreThan(int moreThan) {
        if (mTabLayout.getTabCount() > moreThan) {
            mTabLayout.removeTabAt(mTabLayout.getTabCount() - 1);
            removeTabIfMoreThan(moreThan);
        }
    }

    private void sendProviderDemand(int level, SelectEntity parentEntity) {
        Log.e(TAG, "sendProviderDemand level ==" + level);
        mLoading.show();
        mSelectorEntitiesProviderCallback.onEntitiesProvide(level, parentEntity, mSelectorEntitiesProvider);
    }

    private ISelectorEntitiesProvider mSelectorEntitiesProvider = new ISelectorEntitiesProvider() {
        @Override
        public void sendEntities(List<SelectEntity> dataList) {
            mLoading.dismiss();
            if (dataList.size() > 0) {
                mSelectorEntitiesRootCache.setNodes(dataList);
                if (!selectedEntities.isEmpty()) {
                    selectedEntities.get(0).setNodes(dataList);
                }
                if (mTabLayout.getTabCount() == 0) {
                    addNewTab(mContext.getString(R.string.please_choose), mSelectorEntitiesRootCache, true);
                } else {
                    mAdapter.setNewData(checkSelectedEntity(dataList));
                }
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
         *
         * @param level                    层级
         * @param parentSelectEntity       父级 SelectEntity
         * @param selectorEntitiesProvider 选择数据提供者接口
         */
        void onEntitiesProvide(int level, SelectEntity parentSelectEntity, ISelectorEntitiesProvider selectorEntitiesProvider);

        /**
         * 选择完成
         *
         * @param selectEntityList        已选择的数据
         * @param selectEntitiesWholeName 已选择的数据名称的格式化字符串
         */
        void onEntitiesSelected(List<SelectEntity> selectEntityList, String selectEntitiesWholeName);
    }

    public interface ISelectorEntitiesProvider {
        void sendEntities(List<SelectEntity> dataList);
    }

    public static class Builder {
        private Context context;
        private Dialog loadingDialog;
        private int maxLevel;
        private String title;
        private String nameDivider;
        private int customColor;
        private List<SelectEntity> defaultSelectEntities;
        private List<SelectEntity> childrenOfRoot;
        private SelectorEntitiesProviderCallback selectorEntitiesProviderCallback;

        public Builder(Context context) {
            this.context = context;
            this.nameDivider = "-";
            this.maxLevel = 4;
        }

        public Builder setLoadingDialog(Dialog loadingDialog) {
            this.loadingDialog = loadingDialog;
            return this;
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

        public Builder setCustomColor(int customColor) {
            this.customColor = customColor;
            return this;
        }

        public Builder setDefaultEntities(List<SelectEntity> selectEntities) {
            this.defaultSelectEntities = selectEntities;
            return this;
        }

        public Builder setChildrenOfRoot(List<SelectEntity> rootNodes) {
            this.childrenOfRoot = rootNodes;
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
