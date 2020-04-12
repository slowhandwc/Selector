package com.slow.selector.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择实体
 * @Author wuchao
 * @Date 2020/4/8-11:22 PM
 * @description
 * @email 329187218@qq.com
 * @see
 */
public class SelectEntity {
    String name;
    String id = "";
    String parentId = "0";
    int level;
    String path;
    boolean isChecked;
    /**
     * 子节点实体数据
     */
    List<SelectEntity> childrenEntities = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<SelectEntity> getChildrenEntities() {
        return childrenEntities;
    }

    public void setChildrenEntities(List<SelectEntity> childrenEntities) {
        this.childrenEntities = childrenEntities;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof SelectEntity){
            SelectEntity other = (SelectEntity) obj;
            if(id.equals(other.getId())){
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * 设置节点
     * @param outNodes 外部节点数据
     * @return 是否设置成功
     */
    public boolean setNodes(List<SelectEntity> outNodes){
        if(outNodes.size()>0){
            String outNodesParentId = outNodes.get(0).getParentId();
            if(id.equals(outNodesParentId)){
                setChildrenEntities(outNodes);
                return true;
            } else {
                if(getChildrenEntities().size()>0){
                    for(SelectEntity child:getChildrenEntities()){
                        if(child.setNodes(outNodes)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * 获取选择的节点子数据
     * @param level 层级
     * @param id 节点id
     * @return
     */
    public List<SelectEntity> getSelectNodeChildren(int level,String id){
        if(this.level == level){
            if(this.id.equals(id)){
                if(getChildrenEntities().size()>0){
                    return getChildrenEntities();
                }
                return null;
            } else {
                return null;
            }
        } else {
            if(getChildrenEntities().size()>0){
                for(SelectEntity child:getChildrenEntities()){
                    List<SelectEntity> childNodes = child.getSelectNodeChildren(level,id);
                    if(childNodes!=null){
                        return childNodes;
                    }
                }
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * 是否含有子节点
     * @return
     */
    public boolean isHaveChildren(){
        return getChildrenEntities().size()>0;
    }

    /**
     * 选择子项
     * @param selectEntity 子项
     */
    public boolean chooseChild(SelectEntity selectEntity){
        if(isHaveChildren()){
            int level = getChildrenEntities().get(0).getLevel();
            if(level == selectEntity.getLevel()){
                int indexInChildrenEntities = getChildrenEntities().indexOf(selectEntity);
                if(indexInChildrenEntities>=0){
                    for(SelectEntity item:getChildrenEntities()){
                        item.setChecked(false);
                    }
                    getChildrenEntities().get(indexInChildrenEntities).setChecked(true);
                    return true;
                } else {
                    return false;
                }
            } else {
                for(SelectEntity item:getChildrenEntities()){
                    if(item.chooseChild(selectEntity)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
