package zjy.android.bliveinteract.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.model.GameInfo;
import zjy.android.bliveinteract.widget.WarGameView;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private final List<GameInfo> gameInfos = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_ranking, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameInfo info = gameInfos.get(position);
        holder.nationName.setText(WarGameView.nationName[info.nation]);
        String hp = "城池" + info.capitalNum;
        String terr = "领土" + info.terrNum;
        String warrior = "战士" + info.warriorNum;
        String user = "将军" + info.userNum;
        holder.hp.setText(hp);
        holder.warrior.setText(warrior);
        holder.user.setText(user);
        holder.terr.setText(terr);
        holder.itemView.setBackgroundColor(Color.parseColor(WarGameView.terrColorStr[info.nation]));
    }

    @Override
    public int getItemCount() {
        return gameInfos.size();
    }

    public void update(List<GameInfo> newList) {
        gameInfos.clear();
        gameInfos.addAll(newList);
        notifyItemRangeChanged(0, newList.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nationName, terr, warrior, user, hp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nationName = itemView.findViewById(R.id.nation_name);
            terr = itemView.findViewById(R.id.terr);
            warrior = itemView.findViewById(R.id.warrior);
            user = itemView.findViewById(R.id.user);
            hp = itemView.findViewById(R.id.hp);
        }
    }
}
