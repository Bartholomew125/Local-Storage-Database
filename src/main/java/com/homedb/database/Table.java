package com.homedb.database;

import java.util.Set;

public interface Table<T> {
    public String getName();
    public int insert(T item);
    public int insert(Set<T> items);
    public T select(String itemID);
    public Set<T> select(int limit, int offset, String sortBy);
}
