package com.github.linushp.orm.model;

import java.io.Serializable;
import java.util.List;

public class ScrollPage<T> {
    private List<T> items;
    private boolean hasMore;
    private Serializable nextKey;

    public ScrollPage(List<T> items, boolean hasMore, Serializable nextKey) {
        this.items = items;
        this.hasMore = hasMore;
        this.nextKey = nextKey;
    }


    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public Serializable getNextKey() {
        return nextKey;
    }

    public void setNextKey(Serializable nextKey) {
        this.nextKey = nextKey;
    }
}
