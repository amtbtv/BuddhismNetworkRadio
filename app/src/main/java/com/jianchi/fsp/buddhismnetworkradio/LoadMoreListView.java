package com.jianchi.fsp.buddhismnetworkradio;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadMoreListView extends ListView {
    protected static final String TAG = "LoadMoreListView";
    private View mFooterView;
    private ProgressBar load_more_pb;
    private TextView load_more_tv;
    private OnScrollListener mOnScrollListener;
    private OnLoadMoreListener mOnLoadMoreListener;

    /**
     * If is loading now.
     */
    private boolean mIsLoading = false;
    private boolean mOver = false;

    private int mCurrentScrollState;

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadMoreListView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mFooterView = View.inflate(context, R.layout.load_more_footer, null);
        addFooterView(mFooterView);

        load_more_pb = mFooterView.findViewById(R.id.load_more_pb);
        load_more_tv = mFooterView.findViewById(R.id.load_more_tv);

        hideFooterView();
        /*
         * Must use super.setOnScrollListener() here to avoid override when call
         * this view's setOnScrollListener method
         */
        super.setOnScrollListener(superOnScrollListener);
    }

    /**
     * Hide the load more view(footer view)
     */
    private void hideFooterView() {
        load_more_pb.setVisibility(INVISIBLE);
        load_more_tv.setText(R.string.load_over);
        //mFooterView.setVisibility(View.GONE);
    }

    /**
     * Show load more view
     */
    public void showFooterView() {
        load_more_pb.setVisibility(VISIBLE);
        load_more_tv.setText(R.string.loading);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

    /**
     * Set load more listener, usually you should get more data here.
     *
     * @param listener
     *            OnLoadMoreListener
     * @see OnLoadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

    /**
     * When complete load more data, you must use this method to hide the footer
     * view, if not the footer view will be shown all the time.
     */
    public void onLoadMoreComplete() {
        mOver = true;
        mIsLoading = false;
        hideFooterView();
    }

    public void onLoadMoreFinish() {
        mOver = false;
        mIsLoading = false;
    }

    private OnScrollListener superOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mCurrentScrollState = scrollState;
            // Avoid override when use setOnScrollListener
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(view, firstVisibleItem,
                        visibleItemCount, totalItemCount);
            }
            if (!mIsLoading
                    && (firstVisibleItem + visibleItemCount >= totalItemCount)
                    && mCurrentScrollState != SCROLL_STATE_IDLE
                    && !mOver
            ) {
                loadMore();
            }
        }
    };

    public void loadMore(){
        showFooterView();
        mIsLoading = true;
        if (mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onLoadMore(LoadMoreListView.this);
        }
    }

    /**
     * Interface for load more
     */
    public interface OnLoadMoreListener {
        /**
         * Load more data.
         */
        void onLoadMore(LoadMoreListView loadMoreListView);
    }
}
