package com.seiko.player.widgets.top;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.seiko.player.core.R;
import com.seiko.player.media.VideoInfoTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2018/12/19.
 */
class StreamAdapter extends RecyclerView.Adapter<StreamAdapter.ItemViewHolder> {

    private final int mLayoutResId;
    private final List<VideoInfoTrack> mData;

    StreamAdapter(@LayoutRes int layoutResId, @NonNull List<VideoInfoTrack> data) {
        mLayoutResId = layoutResId;
        mData = new ArrayList<>(data);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int itemViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutResId, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    private OnItemChildClickListener mListener;

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvName;
        private final RadioButton mRadio;

        ItemViewHolder(View view) {
            super(view);
            mTvName = view.findViewById(R.id.track_name_tv);
            mRadio = view.findViewById(R.id.track_select_cb);
            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemChildClick(StreamAdapter.this, v, getAdapterPosition());
                }
            });
        }

        void bind(VideoInfoTrack item) {
            mTvName.setText(item.getName());
            mRadio.setChecked(item.isSelect());
        }
    }

    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        mListener = listener;
    }

    public interface OnItemChildClickListener {
        void onItemChildClick(RecyclerView.Adapter adapter, View view, int position);
    }
}
