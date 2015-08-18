package nope.yuuji.kirisame.widget;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by YuujiSakakibara on 2015/08/16.
 */
public abstract class BaseSuggestionAdapter extends CursorAdapter {

    public static final String[] PRIMARY_KEY = {"_id", "KEY"};

    /**
     * @return View used by adapter
     */
    protected abstract int getViewId();

    protected abstract Object onCreateViewHolder(View view);

    protected abstract void onBindView(Object viewHolder, int pos);

    /**
     * @param pos position in model
     * @return key used as searchable, must be string
     */
    protected abstract String getKey(int pos);

    protected abstract int getModelSize();

    public BaseSuggestionAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public BaseSuggestionAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public BaseSuggestionAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * @param pos position in adapter, not model
     * @return Key used to search in model
     */
    public String getSelectedKey(int pos) {
        Cursor c = getCursor();
        c.moveToPosition(pos);
        return c.getString(c.getColumnIndex(PRIMARY_KEY[1]));
    }

    /**
     * @param pos position in adapter, not model
     * @return real position in model
     */
    public int getSelectedPosition(int pos) {
        Cursor c = getCursor();
        c.moveToPosition(pos);
        return c.getInt(c.getColumnIndex(PRIMARY_KEY[0]));
    }

    @Override
    public final View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(getViewId(), parent, false);
        view.setTag(onCreateViewHolder(view));
        return view;
    }

    @Override
    public final void bindView(View view, Context context, Cursor cursor) {
        onBindView(view.getTag(), cursor.getInt(cursor.getColumnIndex(PRIMARY_KEY[0])));
    }

    @Override
    public final CharSequence convertToString(Cursor cursor) {
        int indexColumnSuggestion = cursor.getColumnIndex(PRIMARY_KEY[1]);
        return cursor.getString(indexColumnSuggestion);
    }

    /**
     * @param query Matched keys with query will be shown
     */
    public void updateQuery(String query) {
        MatrixCursor c = new MatrixCursor(PRIMARY_KEY);
        int total = getModelSize();
        for (int i = 0; i < total; i++) {
            if (isKeyMatchQuery(query, i)) {
                c.addRow(new Object[]{i, getKey(i)});
            }
        }
        changeCursor(c);
    }

    /**
     * Update model, use this instead of notify datasetchange
     */
    public void updateQuery() {
        MatrixCursor c = new MatrixCursor(PRIMARY_KEY);
        int total = getModelSize();
        for (int i = 0; i < total; i++) {
            c.addRow(new Object[]{i, getKey(i)});
        }
        changeCursor(c);
    }

    @Deprecated
    @Override
    /**
     * @deprecated Use updateQuery() instead if you want to update model.
     */
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private boolean isKeyMatchQuery(String query, int i) {
        return (getKey(i).toLowerCase().startsWith(query.toLowerCase()));
    }

}
