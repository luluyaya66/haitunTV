package com.tv.mydiy.epg;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.tv.mydiy.R;
import com.tv.mydiy.util.UiUtils;

public class EpgProgramAdapter extends RecyclerView.Adapter<EpgProgramAdapter.ProgramViewHolder> {
    private List<EpgProgram> programs;
    
    public EpgProgramAdapter(List<EpgProgram> programs) {
        this.programs = programs != null ? new java.util.ArrayList<>(programs) : new java.util.ArrayList<>();
    }
    
    public List<EpgProgram> getPrograms() {
        return programs;
    }
    
    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_epg_program, parent, false);
        return new ProgramViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        EpgProgram program = programs.get(position);
        
        UiUtils.updateTextSafely(holder.title, program.getTitle());
        
        String timeText = "";
        if (program.getStartTime() != null) {
            timeText = EpgManager.formatEpgTimeToHM(program.getStartTime());
        }
        
        UiUtils.updateTextSafely(holder.time, timeText);
        
        boolean isCurrentProgram = isProgramCurrentlyPlaying(program);
        if (isCurrentProgram) {
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_blue_light));
            holder.time.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_blue_light));
        } else {
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.time.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }
    }
    
    private boolean isProgramCurrentlyPlaying(EpgProgram program) {
        if (program == null || program.getStartTime() == null || program.getEndTime() == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long startTime = parseEpgTimeToMillis(program.getStartTime());
        long endTime = parseEpgTimeToMillis(program.getEndTime());
        
        return currentTime >= startTime && currentTime <= endTime;
    }
    
    private long parseEpgTimeToMillis(String epgTime) {
        if (epgTime == null || epgTime.isEmpty()) {
            return 0;
        }
        
        try {
            String timePart = epgTime.trim().split("\\s+")[0];
            if (timePart.length() >= 14) {
                String year = timePart.substring(0, 4);
                String month = timePart.substring(4, 6);
                String day = timePart.substring(6, 8);
                String hour = timePart.substring(8, 10);
                String minute = timePart.substring(10, 12);
                String second = timePart.substring(12, 14);
                
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.set(java.util.Calendar.YEAR, Integer.parseInt(year));
                calendar.set(java.util.Calendar.MONTH, Integer.parseInt(month) - 1);
                calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
                calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(minute));
                calendar.set(java.util.Calendar.SECOND, Integer.parseInt(second));
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                
                return calendar.getTimeInMillis();
            }
        } catch (Exception e) {
        }
        
        return 0;
    }
    
    @Override
    public int getItemCount() {
        return programs != null ? programs.size() : 0;
    }
    
    static class ProgramViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        
        ProgramViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.program_title);
            time = itemView.findViewById(R.id.program_time);
        }
    }
    
    public void updateData(List<EpgProgram> newPrograms) {
        if (newPrograms != null) {
            this.programs = new java.util.ArrayList<>(newPrograms);
        } else {
            this.programs = new java.util.ArrayList<>();
        }
        notifyDataSetChanged();
    }
}