package com.example.ar.Helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ar.MainActivity;
import com.example.ar.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ViewHolder> {

    private List<Model> models;
    private Context mContext;

    public ModelAdapter(Context context, List<Model> models) {
        this.models = models;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ModelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alert_dialog_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModelAdapter.ViewHolder holder, int position) {
        Model model = models.get(position);
        holder.modelNameTextView.setText(model.getName());
        Drawable drawable = ContextCompat.getDrawable(mContext, model.getDrawable() == -1 ? R.drawable.no_model_image : model.getDrawable());
        holder.modelCircleImageView.setImageDrawable(drawable);
        holder.itemView.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) mContext;
            mainActivity.modelHelper.loadModel(position);
            String modelName = "Model : " + model.getName();
            mainActivity.modelNameTextView.setText(modelName);
            if(model.getDrawable() != -1){
                Drawable drawable1 = ContextCompat.getDrawable(mContext, model.getDrawable());
                mainActivity.modelImageView.setImageDrawable(drawable1);
                mainActivity.modelImageView.setVisibility(View.VISIBLE);
            }else mainActivity.modelImageView.setVisibility(View.GONE);
            mainActivity.modelSelectingDialog.dismiss();
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView modelNameTextView;
        CircleImageView modelCircleImageView;

        public ViewHolder(@NonNull  View itemView) {
            super(itemView);
            modelNameTextView = itemView.findViewById(R.id.modelDialogTV);
            modelCircleImageView = itemView.findViewById(R.id.modelDialogIV);
        }
    }
}
