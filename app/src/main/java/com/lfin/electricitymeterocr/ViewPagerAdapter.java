package com.lfin.electricitymeterocr;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lfin.electricitymeterocr.DTO.ElectricityMeterDTO;
import com.lfin.electricitymeterocr.DTO.ElectricityPreprocessingDTO;

import java.util.List;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewHolderPage> {
    // 파일명 리스트
    private List<String> fileNameList;
    // Handler
    private Handler handler;

    /**
    * 생성자
    * */
    public ViewPagerAdapter(List<String> fileNameList, Handler handler) {
        this.fileNameList = fileNameList;
        this.handler = handler;
    }

    @NonNull
    @Override
    public ViewHolderPage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        // item_viewpager.xml 의 view를 가져오기
        View view = LayoutInflater.from(context).inflate(R.layout.item_viewpager, parent, false);
        return new ViewHolderPage(view, handler);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPage holder, int position) {
        if(holder instanceof ViewHolderPage){
            ViewHolderPage viewHolder = (ViewHolderPage) holder;
            viewHolder.onBind(fileNameList.get(position));
        }
    }

    /**
    * 출력할 이미지의 수를 반환
    * */
    @Override
    public int getItemCount() {
        return this.fileNameList.size();
    }

}
