package nope.yuuji.kirisame.ui.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Tkpd_Eka on 8/13/2015.
 */
public class BasicRecyclerView extends RecyclerView{

    public static final int LAYOUT_LINEAR = 1;
    public static final int LAYOUT_GRID = 2;

    public BasicRecyclerView(Context context) {
        super(context);
        setDefaultLayoutManager();
    }

    public BasicRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaultLayoutManager();
    }

    public BasicRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDefaultLayoutManager();
    }

    public void setDefaultLayoutManager(){
        setLayoutManager(new LinearLayoutManager(getContext(), 1, false));
    }

}
