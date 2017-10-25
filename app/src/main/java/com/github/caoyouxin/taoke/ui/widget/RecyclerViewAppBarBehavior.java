package com.github.caoyouxin.taoke.ui.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.github.caoyouxin.taoke.R;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by jasontsang on 10/24/17.
 */

public final class RecyclerViewAppBarBehavior extends AppBarLayout.Behavior {

    private Context context;

    private Map<RecyclerView, RecyclerViewScrollListener> scrollListenerMap = new ArrayMap<>(); //keep scroll listener map, the custom scroll listener also keep the current scroll Y position.

    public RecyclerViewAppBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
        for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
            View view = coordinatorLayout.getChildAt(i);
            if (view instanceof FloatingActionButton && view.getTag().equals(context.getResources().getString(R.string.fab_tag_reset))) {
                view.setVisibility(View.INVISIBLE);
                break;
            }
        }

        if (target instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) target;
            if (scrollListenerMap.get(recyclerView) == null) {
                RecyclerViewScrollListener recyclerViewScrollListener = new RecyclerViewScrollListener(coordinatorLayout, child, this);
                scrollListenerMap.put(recyclerView, recyclerViewScrollListener);
                recyclerView.addOnScrollListener(recyclerViewScrollListener);
                for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
                    View view = coordinatorLayout.getChildAt(i);
                    if (view instanceof FloatingActionButton && view.getTag().equals(context.getResources().getString(R.string.fab_tag_reset))) {
                        view.setOnClickListener(v -> {
                            recyclerViewScrollListener.scrolledY = 0;
                            recyclerViewScrollListener.dragging = false;
                            recyclerViewScrollListener.velocity = 0;
                            recyclerView.scrollToPosition(0);
                            child.setExpanded(true, true);
                        });
                        break;
                    }
                }
            }
            scrollListenerMap.get(recyclerView).setVelocity(velocityY);
            consumed = scrollListenerMap.get(recyclerView).getScrolledY() > 0; //recyclerView only consume the fling when it's not scrolled to the top
        }

        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    private static class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {
        private int scrolledY;
        private boolean dragging;
        private float velocity;
        private WeakReference<CoordinatorLayout> coordinatorLayoutRef;
        private WeakReference<AppBarLayout> childRef;
        private WeakReference<RecyclerViewAppBarBehavior> behaviorWeakReference;

        public RecyclerViewScrollListener(CoordinatorLayout coordinatorLayout, AppBarLayout child, RecyclerViewAppBarBehavior barBehavior) {
            coordinatorLayoutRef = new WeakReference<>(coordinatorLayout);
            childRef = new WeakReference<>(child);
            behaviorWeakReference = new WeakReference<>(barBehavior);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            dragging = newState == RecyclerView.SCROLL_STATE_DRAGGING;
        }

        public void setVelocity(float velocity) {
            this.velocity = velocity;
        }

        public int getScrolledY() {
            return scrolledY;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (coordinatorLayoutRef.get() != null && behaviorWeakReference.get() != null) {
                for (int i = 0; i < coordinatorLayoutRef.get().getChildCount(); i++) {
                    View view = coordinatorLayoutRef.get().getChildAt(i);
                    if (view instanceof FloatingActionButton && view.getTag().equals(behaviorWeakReference.get().context.getResources().getString(R.string.fab_tag_reset))) {
                        view.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            scrolledY += dy;

            if (scrolledY <= 0 && !dragging && childRef.get() != null && coordinatorLayoutRef.get() != null && behaviorWeakReference.get() != null) {
                //manually trigger the fling when it's scrolled at the top
                behaviorWeakReference.get().onNestedFling(coordinatorLayoutRef.get(), childRef.get(), recyclerView, 0, velocity, false);
            }
        }
    }
}