package com.slow.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.slow.selector.Selector;
import com.slow.selector.model.SelectEntity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @Author wuchao
 * @Date 2020/4/12-12:50 PM
 * @description
 * @email 329187218@qq.com
 * @see
 */
public class MainActivity extends AppCompatActivity {
    private List<SelectEntity> mChildrenOfRoot = new ArrayList<>();
    private List<SelectEntity> mSelectEntities = new ArrayList<>();
    Retrofit retrofit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        retrofit = new Retrofit.Builder().baseUrl("http://ycgj.jc-dev.cn/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final Button getAddress = findViewById(R.id.btnGetAddress);
        getAddress.setOnClickListener(v ->
                new Selector.Builder(MainActivity.this)
                        .setTitle("请选择地址")
                        .setCustomColor(getColor(R.color.colorPrimary))
                        .setMaxLevel(3)
                        .setDefaultEntities(mSelectEntities)
                        .setChildrenOfRoot(mChildrenOfRoot)
                        .setOnSelectorEntitiesProviderCallback(new Selector.SelectorEntitiesProviderCallback() {
                            @Override
                            public void onEntitiesProvide(final int level, SelectEntity parentSelectEntity, final Selector.ISelectorEntitiesProvider selectorEntitiesProvider) {
                                switch (level) {
                                    case 1:
                                        getAddress("province", null, addressList -> {
                                            List<SelectEntity> selectEntities = transformSeletEntitesFromAddress(level, addressList);
                                            mChildrenOfRoot.addAll(selectEntities);
                                            selectorEntitiesProvider.sendEntities(selectEntities);
                                        });
                                        break;
                                    case 2:
                                        getAddress("city", parentSelectEntity.getId(), addressList -> selectorEntitiesProvider.sendEntities(transformSeletEntitesFromAddress(level, addressList)));
                                        break;
                                    case 3:
                                        getAddress("area", parentSelectEntity.getId(), addressList -> selectorEntitiesProvider.sendEntities(transformSeletEntitesFromAddress(level, addressList)));
                                        break;
                                    default:
                                        break;
                                }
                            }

                            @Override
                            public void onEntitiesSelected(List<SelectEntity> selectEntityList, String selectEntitiesWholeName) {
                                mSelectEntities.clear();
                                mSelectEntities.addAll(selectEntityList);
                                Toast.makeText(MainActivity.this, selectEntitiesWholeName, Toast.LENGTH_SHORT).show();
                            }
                        }).create().show());
    }

    private void getAddress(String action, String parentId, final GetAddressCallback callback) {
        TestApi testApi = retrofit.create(TestApi.class);
        testApi.getAddress(action, parentId).enqueue(new Callback<AddressResult>() {
            @Override
            public void onResponse(Call<AddressResult> call, Response<AddressResult> response) {
                if (response.isSuccessful()) {
                    AddressResult addressResult = response.body();
                    callback.onSuccess(addressResult.data);
                }
            }

            @Override
            public void onFailure(Call<AddressResult> call, Throwable t) {
                Toast.makeText(MainActivity.this, "error==" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<SelectEntity> transformSeletEntitesFromAddress(int level, List<Address> addressList) {
        List<SelectEntity> selectEntities = new ArrayList<>();
        for (Address address : addressList) {
            SelectEntity selectEntity = new SelectEntity();
            selectEntity.setId(address.id);
            selectEntity.setParentId(address.pid);
            selectEntity.setLevel(level);
            selectEntity.setName(address.name);
            selectEntities.add(selectEntity);
        }
        return selectEntities;
    }

    interface GetAddressCallback {
        void onSuccess(List<Address> addressList);
    }
}
