package com.tv.mydiy.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.tv.mydiy.R;
import com.tv.mydiy.util.UiUtils;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SettingItem> settingItems;
    private final OnSettingItemClickListener listener;
    
    // 定义视图类型
    private static final int VIEW_TYPE_SETTING = 0;
    private static final int VIEW_TYPE_SUB_SETTING = 1;
    private static final int VIEW_TYPE_RADIO_SETTING = 2;
    
    public interface OnSettingItemClickListener {
        void onSettingItemClick(SettingItem item);
    }
    
    public SettingsAdapter(List<SettingItem> settingItems, OnSettingItemClickListener listener) {
        this.settingItems = settingItems;
        this.listener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        SettingItem item = settingItems.get(position);
        if (item.isRadioButton()) {
            return VIEW_TYPE_RADIO_SETTING;
        } else if (item.getType() == SettingItem.SettingItemType.TYPE_SUB_ITEM) {
            return VIEW_TYPE_SUB_SETTING;
        } else {
            return VIEW_TYPE_SETTING;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_RADIO_SETTING:
                View radioView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_radio_setting, parent, false);
                return new RadioSettingsViewHolder(radioView);
            case VIEW_TYPE_SUB_SETTING:
                View subView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_sub_setting, parent, false);
                return new SubSettingsViewHolder(subView);
            default:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_setting, parent, false);
                return new SettingsViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingItem item = settingItems.get(position);
        
        if (holder instanceof RadioSettingsViewHolder) {
            RadioSettingsViewHolder radioHolder = (RadioSettingsViewHolder) holder;
            
            // 检查当前文本是否与新文本相同，避免不必要的UI更新
            UiUtils.updateTextSafely(radioHolder.title, item.getTitle());
            
            // 检查当前选中状态是否与目标状态相同，避免不必要的UI更新
            if (radioHolder.radioButton.isChecked() != item.isChecked()) {
                radioHolder.radioButton.setChecked(item.isChecked());
            }
            
            holder.itemView.setOnClickListener(v -> {
                // 对于单选按钮，我们不应该简单地切换状态，而应该选中它
                // 只有在未选中时才触发监听器
                if (!item.isChecked()) {
                    item.setChecked(true);
                    
                    if (listener != null) {
                        listener.onSettingItemClick(item);
                    }
                    
                    // 立即更新UI以提供即时反馈
                    notifyDataSetChanged();
                }
            });
        } else if (holder instanceof SubSettingsViewHolder) {
            SubSettingsViewHolder subHolder = (SubSettingsViewHolder) holder;
            
            // 检查当前文本是否与新文本相同，避免不必要的UI更新
            UiUtils.updateTextSafely(subHolder.title, item.getTitle());
            
            // 检查当前选中状态是否与目标状态相同，避免不必要的UI更新
            if (subHolder.checkBox.isChecked() != item.isChecked()) {
                subHolder.checkBox.setChecked(item.isChecked());
            }
            
            holder.itemView.setOnClickListener(v -> {
                // 切换复选框状态
                boolean newCheckedState = !item.isChecked();
                item.setChecked(newCheckedState);
                
                if (listener != null) {
                    listener.onSettingItemClick(item);
                }
                
                // 立即更新UI以提供即时反馈
                if (subHolder.checkBox.isChecked() != newCheckedState) {
                    subHolder.checkBox.setChecked(newCheckedState);
                }
            });
        } else {
            SettingsViewHolder settingsHolder = (SettingsViewHolder) holder;
            
            // 检查当前文本是否与新文本相同，避免不必要的UI更新
            UiUtils.updateTextSafely(settingsHolder.title, item.getTitle());
            
            // 不显示任何箭头图标，保持界面一致性
            if (settingsHolder.arrow != null) {
                settingsHolder.arrow.setVisibility(View.GONE);
            }
            
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSettingItemClick(item);
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return settingItems != null ? settingItems.size() : 0;
    }
    
    static class SettingsViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView arrow;
        
        SettingsViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.setting_title);
            arrow = itemView.findViewById(R.id.setting_arrow);
        }
    }
    
    static class SubSettingsViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CheckBox checkBox;
        
        SubSettingsViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sub_setting_title);
            checkBox = itemView.findViewById(R.id.sub_setting_checkbox);
        }
    }
    
    static class RadioSettingsViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RadioButton radioButton;
        
        RadioSettingsViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.radio_setting_title);
            radioButton = itemView.findViewById(R.id.radio_setting_radio_button);
        }
    }
    
    // 获取设置项列表
    public List<SettingItem> getSettingItems() {
        return settingItems;
    }
    
    // 更新数据的方法
    public void updateData(List<SettingItem> newItems) {
        this.settingItems = newItems;
        notifyDataSetChanged();
    }

    // 更新单选按钮组的选择状态
    public void updateSingleSelection(int selectedPosition) {
        if (settingItems != null && selectedPosition >= 0 && selectedPosition < settingItems.size()) {
            for (int i = 0; i < settingItems.size(); i++) {
                SettingItem item = settingItems.get(i);
                if (item.isRadioButton()) {
                    item.setChecked(i == selectedPosition);
                }
            }
            notifyDataSetChanged();
        }
    }
}