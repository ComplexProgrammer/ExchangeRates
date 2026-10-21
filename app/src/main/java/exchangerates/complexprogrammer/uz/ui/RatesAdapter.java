package exchangerates.complexprogrammer.uz.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Set;

import exchangerates.complexprogrammer.uz.R;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.FlagLoader;
import exchangerates.complexprogrammer.uz.services.Formatters;
import exchangerates.complexprogrammer.uz.services.LocaleHelper;

public class RatesAdapter extends ListAdapter<Response, RatesAdapter.Holder> {

    public interface Listener {
        void onRateClick(Response rate);

        void onFavoriteClick(Response rate);
    }

    private static final DiffUtil.ItemCallback<Response> DIFF = new DiffUtil.ItemCallback<Response>() {
        @Override
        public boolean areItemsTheSame(@NonNull Response a, @NonNull Response b) {
            return a.getCcy().equals(b.getCcy());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Response a, @NonNull Response b) {
            return a.getRateValue() == b.getRateValue() && a.getDiffValue() == b.getDiffValue()
                    && a.getDate().equals(b.getDate());
        }
    };

    private final Listener listener;
    private Set<String> favorites = new HashSet<>();

    public RatesAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    public void setFavorites(Set<String> favorites) {
        this.favorites = favorites;
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_rate, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position));
    }

    class Holder extends RecyclerView.ViewHolder {
        final ImageView flag;
        final TextView code, name, rate, diff;
        final ImageButton favorite;

        Holder(View view) {
            super(view);
            flag = view.findViewById(R.id.flag);
            code = view.findViewById(R.id.code);
            name = view.findViewById(R.id.name);
            rate = view.findViewById(R.id.rate);
            diff = view.findViewById(R.id.diff);
            favorite = view.findViewById(R.id.favorite);
        }

        void bind(Response item) {
            Context context = itemView.getContext();
            String currencyName = item.getName(LocaleHelper.getContentLanguage(context));
            String rateText = Formatters.money(context, item.getRateValue());
            String changeText = Formatters.change(context, item);

            FlagLoader.load(flag, item.getCcy());
            code.setText(Formatters.nominal(item));
            name.setText(currencyName);
            rate.setText(rateText);

            // Only the change is colored; an arrow keeps the direction readable without color.
            int color = ContextCompat.getColor(context, Formatters.trendColor(item.getDiffValue()));
            diff.setText(changeText);
            diff.setTextColor(color);
            Drawable rawIcon = androidx.appcompat.content.res.AppCompatResources.getDrawable(context, Formatters.trendIcon(item.getDiffValue()));
            if (rawIcon != null) {
                Drawable icon = DrawableCompat.wrap(rawIcon.mutate());
                int size = (int) (diff.getTextSize() * 1.2f);
                icon.setBounds(0, 0, size, size);
                DrawableCompat.setTint(icon, color);
                diff.setCompoundDrawablesRelative(icon, null, null, null);
            } else {
                diff.setCompoundDrawablesRelative(null, null, null, null);
            }

            boolean isFavorite = favorites.contains(item.getCcy());
            favorite.setImageResource(isFavorite ? R.drawable.ic_star : R.drawable.ic_star_border);
            ImageViewCompat.setImageTintList(favorite, ColorStateList.valueOf(ContextCompat.getColor(context,
                    isFavorite ? R.color.favorite : R.color.text_secondary)));
            favorite.setContentDescription(context.getString(
                    isFavorite ? R.string.favorites_remove : R.string.favorites_add, item.getCcy()));
            favorite.setOnClickListener(v -> listener.onFavoriteClick(item));

            itemView.setContentDescription(context.getString(R.string.row_description,
                    Formatters.nominal(item), currencyName, rateText, changeText));
            itemView.setOnClickListener(v -> listener.onRateClick(item));
        }
    }
}
