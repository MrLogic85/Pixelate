package com.sleepyduck.pixelate4crafting.view;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sleepyduck.pixelate4crafting.R;

/**
 * Created by fredrikmetcalf on 01/02/17.
 */

public class ColorEditList extends LinearLayout {
    private int selectColor;
    private EditState state;
    private OnColorEditListClickListener listener;

    private final OnClickListener addClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onAddColorClicked();
            }
        }
    };

    private final OnClickListener eraseClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            state = EditState.ERASE;
            CardView view = (CardView) getChildAt(1);
            view.setCardBackgroundColor(getResources().getColor(R.color.accent_a200));
            for (int i = 2; i < getChildCount(); ++i) {
                CardView selectView = (CardView) getChildAt(i);
                CardView colorView = (CardView) selectView.findViewById(R.id.color_item_color);
                int cardColor = colorView.getCardBackgroundColor().getDefaultColor();
                selectView.setCardBackgroundColor(cardColor);
                colorView.setVisibility(INVISIBLE);
            }
            if (listener != null) {
                listener.onEraseClicked();
            }
        }
    };

    private final OnClickListener colorClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            CardView view = (CardView) v;
            CardView colorView = (CardView) view.findViewById(R.id.color_item_color);
            int cardColor = colorView.getCardBackgroundColor().getDefaultColor();
            selectColor(cardColor, false);
            if (listener != null) {
                listener.onColorClicked(cardColor);
            }
        }
    };

    public ColorEditList(Context context) {
        this(context, null);
    }

    public ColorEditList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorEditList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutTransition lt = new LayoutTransition();
        lt.disableTransitionType(LayoutTransition.APPEARING);
        setLayoutTransition(lt);

        inflate(context, R.layout.color_list_view_item, this);
        inflate(context, R.layout.color_list_view_item, this);

        if (!isInEditMode()) {
            CardView selectView = (CardView) getChildAt(0).findViewById(R.id.color_item_select);
            selectView.setCardBackgroundColor(Color.WHITE);
            CardView colorView = (CardView) getChildAt(0).findViewById(R.id.color_item_color);
            colorView.setCardBackgroundColor(Color.WHITE);
            ImageView iconView = (ImageView) getChildAt(0).findViewById(R.id.color_item_icon);
            iconView.setImageResource(R.drawable.ic_color_lens_black_24dp);
            selectView.setOnClickListener(addClickListener);

            selectView = (CardView) getChildAt(1).findViewById(R.id.color_item_select);
            selectView.setCardBackgroundColor(getResources().getColor(R.color.accent_a200));
            colorView = (CardView) getChildAt(1).findViewById(R.id.color_item_color);
            colorView.setCardBackgroundColor(Color.WHITE);
            iconView = (ImageView) getChildAt(1).findViewById(R.id.color_item_icon);
            iconView.setImageResource(R.drawable.double_sided_eraser);
            selectView.setOnClickListener(eraseClickListener);
            state = EditState.ERASE;
        }
    }

    public float[] prepareAddColor(int color) {
        View view = selectColor(color, true);
        if (view.getX() > 0) {
            return new float[]{view.getX() + view.getWidth() / 2f, view.getY() + view.getHeight() / 2f};
        } else {
            View child0 = getChildAt(0);
            float margin = getResources().getDimension(R.dimen.padding_tiny);
            return new float[]{(child0.getWidth() + 2 * margin) * 2.5f, child0.getHeight() * 0.5f + margin};
        }
    }

    public void selectItem(int index) {
        getChildAt(index).findViewById(R.id.color_item_select).performClick();
    }

    public void selectColor(int color) {
        selectColor(color, false);
    }

    private View selectColor(int color, boolean prepare) {
        if (state == EditState.ERASE) {
            ((CardView) getChildAt(1)).setCardBackgroundColor(Color.WHITE);
        }
        state = EditState.COLOR;
        View resultView = null;
        for (int i = 2; i < getChildCount(); ++i) {
            CardView view = (CardView) getChildAt(i);
            CardView colorView = (CardView) view.findViewById(R.id.color_item_color);
            int cardColor = colorView.getCardBackgroundColor().getDefaultColor();
            if (cardColor == color) {
                view.setCardBackgroundColor(getContext().getResources().getColor(R.color.accent_a200));
                view.setVisibility(VISIBLE);
                colorView.setCardBackgroundColor(color);
                if (prepare) {
                    view.setCardBackgroundColor(cardColor);
                    colorView.setVisibility(INVISIBLE);
                } else {
                    colorView.setVisibility(VISIBLE);
                }
                resultView = view;
            } else {
                view.setCardBackgroundColor(cardColor);
                colorView.setVisibility(INVISIBLE);
            }
        }
        if (resultView == null) {
            CardView selectView = (CardView) inflate(getContext(), R.layout.color_list_view_item, null);
            CardView colorView = (CardView) selectView.findViewById(R.id.color_item_color);
            colorView.setCardBackgroundColor(color);
            selectView.setOnClickListener(colorClickListener);
            if (prepare) {
                selectView.setVisibility(INVISIBLE);
            }
            addView(selectView, 2, getChildAt(0).getLayoutParams());
            if (getChildCount() == 7) {
                removeViewAt(6);
            }
            resultView = selectView;
        }

        selectColor = color;
        return resultView;
    }

    public void setOnColorEditListListener(OnColorEditListClickListener listListener) {
        this.listener = listListener;
    }

    public EditState getState() {
        return state;
    }

    public int getColor() {
        return selectColor;
    }

    public enum EditState {
        COLOR,
        ERASE
    }

    public interface OnColorEditListClickListener {
        void onColorClicked(int color);

        void onEraseClicked();

        void onAddColorClicked();
    }
}
